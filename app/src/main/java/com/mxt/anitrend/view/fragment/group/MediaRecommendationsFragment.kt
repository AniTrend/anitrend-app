package com.mxt.anitrend.view.fragment.group

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.RecommendationAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.data.mapper.toPageInfo
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.domain.model.RecommendationPageResult
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.media.MediaActionUtil
import com.mxt.anitrend.view.activity.detail.MediaActivity
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaRecommendationsFragment : FragmentBaseList<RecommendationItemUiModel, RecommendationPageResult>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    private val settings: Settings by inject()

    private val mediaRecommendationsViewModel: MediaRecommendationsViewModel by viewModel()

    private var recommendationAdapter: RecommendationAdapter? = null

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle): MediaRecommendationsFragment = MediaRecommendationsFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        arguments?.let { args ->
            mediaId = args.getLong(KeyUtil.arg_id)
            mediaType = args.getString(KeyUtil.arg_mediaType)
        }
        isPager = true
        mColumnSize = R.integer.grid_giphy_x3
        recommendationAdapter =
            RecommendationAdapter(
                context = ctx,
                onOpenMedia = ::openMedia,
                onLongPressMedia = ::onLongPressMedia,
            )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaRecommendationsViewModel.state.collect { state ->
                    when (state) {
                        is MediaRecommendationsViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout / progress layout in the base class
                        }
                        is MediaRecommendationsViewModel.UiState.Success -> {
                            handleSuccess(state.items, state.pageInfo)
                        }
                        is MediaRecommendationsViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        showLoading()
        val adapter = recommendationAdapter
        if (adapter == null || adapter.itemCount < 1) {
            onRefresh()
        } else {
            updateUI()
        }
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        val adapter = recommendationAdapter ?: return
        if (adapter.itemCount > 0) {
            if (recyclerView.adapter !== adapter) {
                recyclerView.adapter = adapter
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false)
            } else if (swipeRefreshLayout.isLoading()) {
                swipeRefreshLayout.setLoading(false)
            }
            showContent()
        } else {
            showEmpty(getString(R.string.layout_empty_response))
        }
    }

    override fun makeRequest() {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        mediaRecommendationsViewModel.load(
            mediaId = mediaId,
            type = type,
            isAdult = isAdult,
            page = mScrollListener.currentPage.takeIf { it > 1 },
        )
    }

    private fun handleSuccess(
        items: List<RecommendationItemUiModel>,
        pageInfo: com.mxt.anitrend.domain.model.PageInfoRecord?,
    ) {
        val adapter = recommendationAdapter ?: return
        pageInfo?.toPageInfo()?.let { setPageInfo(it) }
        if (items.isEmpty() && adapter.itemCount > 0) {
            setLimitReached()
            updateUI()
        } else {
            adapter.submitList(items) { updateUI() }
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: RecommendationPageResult?) = Unit

    private fun openMedia(
        target: View,
        item: RecommendationItemUiModel,
    ) {
        val host = activity ?: return
        val intent = MediaActivity.newIntent(host, item.mediaId, item.mediaType)
        CompatUtil.startRevealAnim(host, target, intent)
    }

    private fun onLongPressMedia(
        target: View,
        item: RecommendationItemUiModel,
    ): Boolean {
        if (settings.isAuthenticated) {
            val host = activity ?: return false
            mediaActionUtil =
                MediaActionUtil
                    .Builder()
                    .setId(item.mediaId)
                    .build(host)
            mediaActionUtil.startSeriesAction()
            return true
        }
        context?.let {
            NotifyUtil
                .makeText(
                    it,
                    R.string.info_login_req,
                    R.drawable.ic_group_add_grey_600_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
        }
        return true
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<RecommendationItemUiModel>,
    ) = Unit

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<RecommendationItemUiModel>,
    ) = Unit
}
