package com.mxt.anitrend.model.entity.group

import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2018/02/18.
 * EntityGroup for grouping items in a recycler view
 */
open class RecyclerItem {
    @get:KeyUtil.RecyclerViewType
    @setparam:KeyUtil.RecyclerViewType
    var contentType: Int = 0

    var subGroupTitle: String? = null
}
