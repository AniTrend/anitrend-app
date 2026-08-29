package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for staff destinations.
 *
 * Identity-only: carries the stable staff id. The tri-state `onList` media-list
 * filter remains local screen state and is not part of the destination identity;
 * the legacy `arg_onList` key is retained only for compatibility inputs.
 *
 * @property staffId Stable AniList staff id; the destination resolves current state by this id.
 */
@Parcelize
data class StaffScreenParam(
    val staffId: Long,
) : ScreenParam
