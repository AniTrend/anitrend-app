package com.mxt.anitrend.ui.media

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.markdown.RegexUtil

internal data class FeedAttachment(
    val type: String,
    val url: String,
    val imageUrl: String,
    val centerCrop: Boolean,
    val showPlayback: Boolean,
) {
    companion object {
        fun from(type: String, url: String): FeedAttachment = when (type) {
            RegexUtil.KEY_IMG -> FeedAttachment(type, url, url, centerCrop = false, showPlayback = false)
            RegexUtil.KEY_YOU -> FeedAttachment(type, url, RegexUtil.getYoutubeThumb(url), centerCrop = true, showPlayback = true)
            else -> FeedAttachment(type, url, RegexUtil.NO_THUMBNAIL, centerCrop = true, showPlayback = true)
        }
    }
}

@Composable
internal fun FeedAttachmentCarousel(
    attachments: List<FeedAttachment>,
    onOpen: (FeedAttachment) -> Unit,
    onPageChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { attachments.size })
    val currentPageChanged by rememberUpdatedState(onPageChanged)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPageChanged(it) }
    }
    HorizontalPager(
        state = pagerState,
        key = { index -> "$index:${attachments[index].type}:${attachments[index].url}" },
        modifier = modifier.fillMaxWidth().testTag("feed-attachment-pager"),
    ) { index ->
        val attachment = attachments[index]
        FeedSlideRow(
            imageUrl = attachment.imageUrl,
            centerCrop = attachment.centerCrop,
            showPlayback = attachment.showPlayback,
            roundedCorners = true,
            onClick = { onOpen(attachment) },
        )
    }
}

@Preview(name = "Feed attachment pager", widthDp = 360)
@Composable
private fun FeedAttachmentCarouselPreview() {
    AniTrendTheme {
        FeedAttachmentCarousel(
            attachments = listOf(FeedAttachment.from(RegexUtil.KEY_WEB, "https://example.com/video.mp4")),
            onOpen = {},
            onPageChanged = {},
        )
    }
}
