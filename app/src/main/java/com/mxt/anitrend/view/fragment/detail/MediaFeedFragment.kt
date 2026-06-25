package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment

/**
 * Created by max on 2018/03/24.
 * Media feed list fragment for media types, both anime and manga
 */
class MediaFeedFragment : FeedListFragment() {
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

    override fun makeRequest() {
        val ctx = context ?: return
        viewModel?.params?.apply {
            putLong(KeyUtil.arg_mediaId, arguments?.getLong(KeyUtil.arg_mediaId) ?: 0)
            putBoolean(KeyUtil.arg_isFollowing, arguments?.getBoolean(KeyUtil.arg_isFollowing) ?: true)
            putInt(KeyUtil.arg_page, presenter.currentPage)
            putInt(KeyUtil.arg_page_limit, arguments?.getInt(KeyUtil.arg_page_limit) ?: KeyUtil.PAGING_LIMIT)
        }
        viewModel?.requestData(KeyUtil.MEDIA_SOCIAL_REQ, ctx)
    }
}
