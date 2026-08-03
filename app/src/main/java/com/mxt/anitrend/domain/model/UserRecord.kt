package com.mxt.anitrend.domain.model

/**
 * Immutable canonical representation of a user profile in the in-memory stores.
 *
 * Reuses the existing domain value types (avatar and banner as URL strings,
 * matching [UserSummaryRecord]) and mirrors the mutable entity fields on
 * `UserBase`. The revision drives deterministic upsert and stale-response
 * rejection inside [com.mxt.anitrend.data.store.user.UserStore].
 */
data class UserRecord(
    val id: Long,
    val name: String?,
    val avatar: String?,
    val banner: String?,
    val isFollowing: Boolean,
    val revision: Long,
)
