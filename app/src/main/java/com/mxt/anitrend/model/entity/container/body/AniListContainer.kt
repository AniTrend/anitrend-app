package com.mxt.anitrend.model.entity.container.body

import co.anitrend.retrofit.graphql.model.attribute.GraphError

data class AniListContainer<T>(
    val data: DataContainer<T>?,
    val errors: List<GraphError>?,
)
