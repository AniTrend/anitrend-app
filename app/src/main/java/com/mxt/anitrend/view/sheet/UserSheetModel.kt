package com.mxt.anitrend.view.sheet

import android.os.Parcelable
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import kotlinx.parcelize.Parcelize

/**
 * Parcel-safe immutable boundary model for the users bottom sheet ([BottomSheetUsers]).
 *
 * The legacy [UserBase] entity marks its `id` and `avatar` as `@IgnoredOnParcel`, so a
 * bundle round trip silently zeroes the id (and drops the avatar). That breaks follow
 * dispatch (userId 0 is sent) and [com.mxt.anitrend.data.store.user.UserStore] rebinding
 * (a record id never matches a zeroed row id). [BottomSheetUsers.Builder] therefore
 * converts every item through this model before parceling, and the sheet converts back
 * with [toUserBase], keeping the real AniList user id and avatar alive across the boundary.
 *
 * [isFollowing] is carried through the round trip so the server-reported follow state
 * added to [com.mxt.anitrend.domain.model.UserSummaryRecord] is preserved until the
 * canonical [com.mxt.anitrend.data.store.user.UserStore] commits authoritative state.
 */
@Parcelize
data class UserSheetModel(
    val id: Long,
    val name: String?,
    val avatar: String?,
    val isFollowing: Boolean = false,
) : Parcelable

fun UserBase.toUserSheetModel(): UserSheetModel = UserSheetModel(
    id = id,
    name = name,
    avatar = avatar?.large ?: avatar?.medium ?: avatar?.extraLarge,
    isFollowing = isFollowing,
)

fun UserSheetModel.toUserBase(): UserBase {
    val avatarUrl = avatar
    return UserBase(name = name).apply {
        id = this@toUserBase.id
        avatar = avatarUrl?.let {
            ImageBase(
                extraLarge = it,
                large = it,
                medium = it,
            )
        }
        isFollowing = this@toUserBase.isFollowing
    }
}
