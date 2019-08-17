package com.mxt.anitrend.model.entity.container.body

import io.github.wax911.library.model.attribute.GraphError

data class AniListContainer<T>(
        val data: DataContainer<T>?,
        val errors: List<GraphError>?
)
