package com.mxt.anitrend.navigation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Legacy parcelable draft argument for the feed composer bottom sheet.
 *
 * This is NOT identity navigation: it is a local draft/navigation bundle carried on
 * the legacy `arg_model` parcelable channel (see BottomSheetComposer), predating the
 * ScreenParam wire-key contract. It deliberately does not carry a canonical
 * [com.mxt.anitrend.model.entity.anilist.FeedList] or
 * [com.mxt.anitrend.model.entity.base.UserBase]; the save routes through the domain
 * interactor and store.
 *
 * @property feedId Stable feed id being edited, or null when composing a new feed.
 * @property draftText Initial editor text taken from the feed being edited.
 * @property recipientId Stable recipient user id for message-feed mode.
 * @property recipientName Recipient display name for message-feed mode.
 */
@Parcelize
data class FeedComposerScreenParam(
    val feedId: Long? = null,
    val draftText: String? = null,
    val recipientId: Long? = null,
    val recipientName: String? = null,
) : Parcelable
