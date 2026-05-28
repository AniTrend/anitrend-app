package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import com.mxt.anitrend.util.KeyUtil
import io.github.wax911.library.model.request.QueryContainerBuilder

class MediaLatestList : MediaBrowseFragment() {

    companion object {
        @JvmStatic
        fun newInstance(params: Bundle, queryContainer: QueryContainerBuilder): MediaLatestList {
            val args = Bundle(params).apply {
                putParcelable(KeyUtil.arg_graph_params, queryContainer)
            }
            return MediaLatestList().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFilterableEnabled = false
    }

    override fun makeRequest() {
        val ctx = context ?: return
        val bundle = viewModel?.params ?: Bundle.EMPTY
        queryContainer.putVariable(KeyUtil.arg_page, presenter.currentPage)
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        viewModel?.requestData(KeyUtil.MEDIA_BROWSE_REQ, ctx)
    }
}
