package com.mxt.anitrend.ui.paging

import com.mxt.anitrend.model.entity.container.attribute.PageInfo

/** Page bookkeeping only. Compose hosts own viewport detection and request gating. */
open class ManualPageState {
    var currentPage = 1
    var currentOffset = 0
    private var pageInfo: PageInfo? = null

    fun onRefreshPage() {
        currentPage = 1
        currentOffset = 0
        pageInfo = null
    }

    fun setPageInfo(pageInfo: PageInfo?) {
        this.pageInfo = pageInfo
    }

    fun getPageInfo(): PageInfo? = pageInfo
}
