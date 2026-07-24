package com.mxt.anitrend.view.fragment.group

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.recycler.group.GroupSeriesAdapter
import com.mxt.anitrend.base.custom.fragment.FragmentBaseList
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.RecommendationBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.model.entity.group.RecyclerItem
import com.mxt.anitrend.presenter.fragment.MediaPresenter
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

class MediaRecommendationsFragment : FragmentBaseList<RecyclerItem, ConnectionContainer<PageContainer<RecommendationBase>>, MediaPresenter>() {
    @KeyUtil.MediaType
    private var mediaType: String? = null
    private var mediaId: Long = 0

    private val settings: Settings by inject()

    private val mediaRecommendationsViewModel: MediaRecommendationsViewModel by viewModel()

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
        mColumnSize = R.integer.grid_giphy_x3
        mAdapter = GroupSeriesAdapter(ctx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaRecommendationsViewModel.state.collect { state ->
                    when (state) {
                        is MediaRecommendationsViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaRecommendationsViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaRecommendationsViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun updateUI() {
        setSwipeRefreshLayoutEnabled(false)
        injectAdapter()
    }

    override fun makeRequest() {
        val type = mediaType?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
        val isAdult: Boolean? = if (settings.displayAdultContent) null else false
        mediaRecommendationsViewModel.load(mediaId = mediaId, type = type, isAdult = isAdult)
    }

    private fun handleSuccess(content: ConnectionContainer<PageContainer<RecommendationBase>>) {
        if (!content.isEmpty) {
            if (content.connection.hasPageInfo()) {
                setPageInfo(content.connection.pageInfo)
            }
            val entityMap: List<RecyclerItem> =
                content.connection.pageData.mapNotNull { it.mediaRecommendation }
            onPostProcessed(entityMap)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }

    /** No-op: StateFlow collector above handles the response. */
    override fun onChanged(value: ConnectionContainer<PageContainer<RecommendationBase>>?) = Unit

    override fun onItemClick(
        target: View,
        data: IndexedValue<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                val media = data.value as? MediaBase ?: return
                val host = activity ?: return
                val intent =
                    Intent(host, MediaActivity::class.java).apply {
                        putExtra(KeyUtil.arg_id, media.id)
                        putExtra(KeyUtil.arg_mediaType, media.type)
                    }
                CompatUtil.startRevealAnim(host, target, intent)
            }
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<RecyclerItem>,
    ) {
        when (target.id) {
            R.id.container -> {
                if (settings.isAuthenticated) {
                    val media = data.value as? MediaBase ?: return
                    val host = activity ?: return
                    mediaActionUtil =
                        MediaActionUtil
                            .Builder()
                            .setId(media.id)
                            .build(host)
                    mediaActionUtil.startSeriesAction()
                } else {
                    context?.let {
                        NotifyUtil
                            .makeText(
                                it,
                                R.string.info_login_req,
                                R.drawable.ic_group_add_grey_600_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            }
        }
    }
}
