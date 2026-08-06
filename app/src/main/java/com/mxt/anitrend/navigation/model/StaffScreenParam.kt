package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for staff destinations.
 *
 * Identity-only: carries the stable staff id. The tri-state `onList` media-list
 * filter stays on the legacy `arg_onList` transitional channel until the pager
 * fragments migrate to typed arguments (Phase 2).
 *
 * @property staffId Stable AniList staff id; the destination resolves current state by this id.
 */
@Parcelize
data class StaffScreenParam(
    val staffId: Long,
) : ScreenParam
