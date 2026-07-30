package com.mxt.anitrend.adapter.recycler.index

import com.mxt.anitrend.domain.model.MediaListItemUiModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaListItemUiModelDiffTest {

    @Test
    fun `diff callback matches items by entry id and media id`() {
        val first = createItem(id = 1L, mediaId = 100L, progress = 5)
        val second = createItem(id = 1L, mediaId = 100L, progress = 9)

        assertTrue(MediaListAdapter.DIFF_CALLBACK.areItemsTheSame(first, second))
        assertFalse(MediaListAdapter.DIFF_CALLBACK.areContentsTheSame(first, second))
    }

    @Test
    fun `diff callback detects identical contents`() {
        val first = createItem(id = 1L, mediaId = 100L, progress = 5)
        val second = createItem(id = 1L, mediaId = 100L, progress = 5)

        assertTrue(MediaListAdapter.DIFF_CALLBACK.areItemsTheSame(first, second))
        assertTrue(MediaListAdapter.DIFF_CALLBACK.areContentsTheSame(first, second))
    }

    private fun createItem(
        id: Long,
        mediaId: Long,
        progress: Int,
    ): MediaListItemUiModel = MediaListItemUiModel(
        id = id,
        mediaId = mediaId,
        status = "CURRENT",
        progress = progress,
        progressVolumes = 0,
        score = 8.0,
        repeat = 0,
        mediaTitle = "Title",
        mediaTitleEnglish = "Title",
        mediaTitleOriginal = null,
        mediaCoverImage = null,
        mediaType = "ANIME",
        mediaFormat = "TV",
        mediaStatus = "RELEASING",
        mediaEpisodes = 12,
        mediaChapters = 0,
        mediaVolumes = 0,
        mediaStartDate = null,
        nextAiringEpisode = null,
        mediaIsFavourite = false,
        isIncrementPending = false,
        isDeletePending = false,
        canIncrement = true,
    )
}
