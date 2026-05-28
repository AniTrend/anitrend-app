package com.mxt.anitrend.model.entity.container.body

import com.mxt.anitrend.model.entity.container.attribute.Edge
import com.mxt.anitrend.util.CompatUtil

/**
 * Edge Connection Container
 * T - Relation type
 */
class EdgeContainer<T : Edge<*>> : Container() {

    var edges: List<T> = emptyList()

    override val isEmpty: Boolean
        get() = CompatUtil.isEmpty(edges)
}
