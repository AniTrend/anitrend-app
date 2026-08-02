package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for comment and feed-reply destinations.
 *
 * @property feedId Stable feed id; the destination resolves current state by this id.
 */
@Parcelize
data class CommentScreenParam(
    val feedId: Long,
) : ScreenParam
