package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the feed composer bottom sheet.
 *
 * Carries only the identity and draft state required to reconstruct the editor:
 * - [feedId] is the stable feed id being edited, or null when composing a new feed.
 * - [draftText] is the initial editor text taken from the feed being edited.
 * - [recipientId] and [recipientName] identify the message recipient for message-feed
 *   mode (new message, or editing a previously sent message).
 *
 * It deliberately does not carry a canonical
 * [com.mxt.anitrend.model.entity.anilist.FeedList] or
 * [com.mxt.anitrend.model.entity.base.UserBase]. The destination resolves current
 * state by identity and the save routes through the domain interactor and store.
 */
@Parcelize
data class FeedComposerScreenParam(
    val feedId: Long? = null,
    val draftText: String? = null,
    val recipientId: Long? = null,
    val recipientName: String? = null,
) : ScreenParam
