package com.mxt.anitrend.domain.model

data class UserSummaryRecord(
    val id: Long,
    val name: String?,
    val avatar: String?,
    val siteUrl: String?,
    /**
     * Server-reported follow state of the summarized user for the current viewer.
     * Defaults to false so existing construction sites stay source-compatible; the
     * canonical committed follow state remains owned exclusively by
     * [com.mxt.anitrend.data.store.user.UserStore].
     */
    val isFollowing: Boolean = false,
)
