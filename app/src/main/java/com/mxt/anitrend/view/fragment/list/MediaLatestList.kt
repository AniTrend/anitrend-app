package com.mxt.anitrend.view.fragment.list

import android.os.Bundle
import com.mxt.anitrend.util.KeyUtil

class MediaLatestList : MediaBrowseFragment() {
    companion object {
        @JvmStatic
        fun newInstance(params: Bundle): MediaLatestList {
            val args = Bundle(params)
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
        val bundle = viewModel?.params ?: return
        bundle.putString(KeyUtil.arg_mediaType, requestArgs.getString(KeyUtil.arg_mediaType))
        bundle.putString(KeyUtil.arg_sort, requestArgs.getString(KeyUtil.arg_sort))
        bundle.putInt(KeyUtil.arg_page, presenter.currentPage)
        bundle.putInt(KeyUtil.arg_page_limit, requestArgs.getInt(KeyUtil.arg_page_limit, KeyUtil.PAGING_LIMIT))
        bundle.applyAdultContentPreference(
            displayAdultContent = presenter.settings.displayAdultContent,
            configuredValue = requestArgs.takeIf { it.containsKey(KeyUtil.arg_isAdult) }?.getBoolean(KeyUtil.arg_isAdult),
        )
        viewModel?.requestData(KeyUtil.MEDIA_BROWSE_REQ, ctx)
    }
}
