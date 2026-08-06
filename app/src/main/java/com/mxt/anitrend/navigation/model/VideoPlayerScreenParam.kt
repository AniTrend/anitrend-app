package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the video player destination.
 *
 * @property url Stable content URL to play.
 */
@Parcelize
data class VideoPlayerScreenParam(
    val url: String,
) : ScreenParam
