package com.mxt.anitrend.domain.model

import com.mxt.anitrend.graphql.generated.LikeableType

data class ToggleLikeCommand(
    val id: Long,
    val likeableType: LikeableType,
)
