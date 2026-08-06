package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the Giphy preview destination.
 *
 * @property url Stable Giphy media URL to preview.
 */
@Parcelize
data class GiphyPreviewScreenParam(
    val url: String,
) : ScreenParam
