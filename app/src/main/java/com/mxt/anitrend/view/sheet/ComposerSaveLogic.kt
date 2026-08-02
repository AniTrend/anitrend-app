package com.mxt.anitrend.view.sheet

import com.mxt.anitrend.domain.feed.interactor.SaveFeedRequest
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import com.mxt.anitrend.util.KeyUtil

/**
 * Maps a composer mode, its identity/draft parameter, and the editor text to the domain
 * save request handled by [com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor].
 *
 * This is the single conversion point for feed saves from the composer sheet. It never
 * receives or mutates a canonical [com.mxt.anitrend.model.entity.anilist.FeedList]; the
 * sheet only holds the immutable [FeedComposerScreenParam], so a failed submission can
 * never modify an adapter-held or parceled feed instance.
 *
 * @param requestType composer mode (text or message feed)
 * @param composerParam identity and draft state, or null when absent
 * @param text the editor text being submitted
 * @return the domain save request, or null when the mode is not a feed save mode
 */
internal fun buildComposerSaveRequest(
    @KeyUtil.RequestType requestType: Int,
    composerParam: FeedComposerScreenParam?,
    text: String,
): SaveFeedRequest? = when (requestType) {
    KeyUtil.MUT_SAVE_TEXT_FEED ->
        SaveFeedRequest.Text(
            id = composerParam?.feedId,
            text = text,
        )
    KeyUtil.MUT_SAVE_MESSAGE_FEED ->
        SaveFeedRequest.Message(
            id = composerParam?.feedId,
            message = text,
            recipientId = composerParam?.recipientId ?: 0L,
        )
    else -> null
}

/**
 * Extracts the edit identity from an immutable [FeedItemUiModel] into the typed
 * [FeedComposerScreenParam], preserving any recipient identity already carried by
 * [current]. This is the record/UI-model bridge used by the feed list edit path:
 * only the stable feed id and the draft text are copied, never the model itself.
 */
internal fun FeedItemUiModel.toComposerParam(current: FeedComposerScreenParam = FeedComposerScreenParam()): FeedComposerScreenParam =
    current.copy(
        feedId = id,
        draftText = feedText,
    )
