package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the YouTube embed destination.
 *
 * Identity-only: the embed resolves its video URL from the stable trailer id and
 * site, so the [com.mxt.anitrend.model.entity.anilist.meta.MediaTrailer] entity is
 * never parceled into the fragment bundle.
 *
 * @property trailerId Stable trailer id (e.g. YouTube video id).
 * @property site Trailer host site (e.g. "youtube").
 */
@Parcelize
data class TrailerScreenParam(
    val trailerId: String,
    val site: String,
) : ScreenParam
