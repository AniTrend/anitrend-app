package com.mxt.anitrend.domain.model

/**
 * Describes the intent to toggle the follow state of a user.
 *
 * Data type only for now; repository calls and widget wiring are introduced by
 * a later phase.
 */
data class ToggleUserFollowCommand(
    val userId: Long,
)
