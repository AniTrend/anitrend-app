package com.mxt.anitrend.model.entity.container.body

import com.mxt.anitrend.model.entity.container.attribute.PageInfo

abstract class Container {

    lateinit var pageInfo: PageInfo

    fun hasPageInfo(): Boolean = ::pageInfo.isInitialized

    abstract val isEmpty: Boolean
}
