package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.UserRecord
import com.mxt.anitrend.model.entity.base.UserBase

/**
 * Maps the existing mutable user entity representation ([UserBase], including
 * its [com.mxt.anitrend.model.entity.anilist.User] subtype) to the immutable
 * [UserRecord] consumed by the in-memory user store. No entity is changed and
 * no ObjectBox entity is added by this lane.
 */
fun UserBase.toUserRecord(revision: Long = 0L): UserRecord = UserRecord(
    id = id,
    name = name,
    avatar = avatar?.large ?: avatar?.medium ?: avatar?.extraLarge,
    banner = bannerImage,
    isFollowing = isFollowing,
    revision = revision,
)
