package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import com.mxt.anitrend.viewmodel.MediaFeedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by max on 2018/03/24.
 * Media feed list fragment for media types, both anime and manga
 */
class MediaFeedFragment : FeedListFragment() {

    private val mediaFeedViewModel: MediaFeedViewModel by viewModel()

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaFeedFragment {
            val args =
                Bundle(params).apply {
                    putLong(KeyUtil.arg_mediaId, params.getLong(KeyUtil.arg_id))
                    putBoolean(KeyUtil.arg_isFollowing, true)
                    putInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT)
                }
            return MediaFeedFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMenuDisabled = true
        isFeed = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaFeedViewModel.state.collect { state ->
                    when (state) {
                        is MediaFeedViewModel.UiState.Loading -> {
                            // Loading is handled by swipeRefreshLayout in the base class
                        }
                        is MediaFeedViewModel.UiState.Success -> {
                            handleSuccess(state.content)
                        }
                        is MediaFeedViewModel.UiState.Error -> {
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    override fun makeRequest() {
        val args = arguments ?: return
        mediaFeedViewModel.load(
            mediaId = args.getLong(KeyUtil.arg_mediaId, 0),
            isFollowing = args.getBoolean(KeyUtil.arg_isFollowing, true),
            page = mScrollListener.currentPage,
            pageLimit = args.getInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT),
        )
    }

    private fun handleSuccess(value: PageContainer<FeedList>) {
        if (value.hasPageInfo()) {
            setPageInfo(value.pageInfo)
        }
        if (!value.isEmpty) {
            val filtered = value.pageData.filter { !it.type.isNullOrBlank() }
            mScrollListener.getPageInfo()?.perPage = filtered.size
            onPostProcessed(filtered)
        } else {
            onPostProcessed(emptyList())
        }
        if (mAdapter.itemCount < 1) {
            onPostProcessed(null)
        }
    }
}
