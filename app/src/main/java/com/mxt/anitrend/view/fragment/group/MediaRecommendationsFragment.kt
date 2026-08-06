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
import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
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
        /**
         * Resolves the media identity from the fragment arguments.
         *
         * The typed [MediaScreenParam] wins when present and valid; otherwise the
         * legacy [KeyUtil.arg_id] / [KeyUtil.arg_mediaType] extras are bridged with
         * their exact raw values (0 or negative ids pass through, mirroring the
         * pre-refactor getter). A typed param present but invalid falls back to the
         * legacy raw values.
         */
        fun fromBundle(bundle: Bundle?): MediaScreenParam? = resolve(
            typed = bundle?.screenParam<MediaScreenParam>(),
            legacyId = bundle?.getLong(KeyUtil.arg_id) ?: 0L,
            legacyType = bundle?.getString(KeyUtil.arg_mediaType),
        )

        @VisibleForTesting
        internal fun resolve(typed: MediaScreenParam?, legacyId: Long, legacyType: String?): MediaScreenParam? {
            typed?.let { param ->
                if (param.mediaId > 0) return param
                // Typed param present but invalid: fall through to the exact raw legacy values.
            }
            return MediaScreenParam(mediaId = legacyId, mediaType = legacyType)
        }

        @JvmStatic
        fun newInstance(args: Bundle): MediaRecommendationsFragment = MediaRecommendationsFragment().apply {
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext()
        fromBundle(arguments)?.let { args ->
            mediaId = args.mediaId
            mediaType = args.mediaType
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
        super.onStart()
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
