package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the image preview destination.
 *
 * @property url Stable content URL to preview.
 */
@Parcelize
data class ImagePreviewScreenParam(
    val url: String,
) : ScreenParam
