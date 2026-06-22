package com.mxt.anitrend.view.fragment.detail

import android.os.Bundle
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.view.fragment.list.FeedListFragment
import co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder

/**
 * Created by max on 2018/03/24.
 * Media feed list fragment for media types, both anime and manga
 */
class MediaFeedFragment : FeedListFragment() {

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle, queryContainer: QueryContainerBuilder): MediaFeedFragment {
            val args = Bundle(params).apply {
                putParcelable(KeyUtil.arg_graph_params, queryContainer)
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
        queryContainer.putVariable(KeyUtil.arg_page, presenter.currentPage)
        viewModel?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_SOCIAL_REQ, ctx)
    }
}
