package com.mxt.anitrend.ui.mediabrowse

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCard
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import com.mxt.anitrend.util.KeyUtil

internal const val MEDIA_BROWSE_CARD_TAG = "media-browse-card"
internal const val MEDIA_BROWSE_AIRING_TAG = "media-browse-airing"
internal const val MEDIA_BROWSE_RATING_TAG = "media-browse-rating"
internal const val MEDIA_BROWSE_STATUS_TAG = "media-browse-status"

/** Stateless Compose grid host for the legacy browse row layouts. */
@Composable
internal fun MediaBrowseGrid(
    items: List<MediaBase>,
    isCompatType: Boolean,
    style: Int,
    columnsResource: Int,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onMediaClick: (MediaBase) -> Unit,
    onMediaLongClick: (MediaBase) -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyGridLoadMoreEffect(listState, onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(columnsResource)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(
            items = items,
            key = { item -> "media:${item.id}" },
            contentType = { "media-browse" },
        ) { item ->
            BrowseMediaCard(
                media = item,
                isCompatType = isCompatType,
                style = style,
                onMediaClick = { onMediaClick(item) },
                onMediaLongClick = { onMediaLongClick(item) },
                modifier = Modifier.testTag(MEDIA_BROWSE_CARD_TAG),
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BrowseMediaCard(
    media: MediaBase,
    isCompatType: Boolean,
    style: Int,
    onMediaClick: () -> Unit,
    onMediaLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state = MediaSeriesCardUiState.from(context, media)

    if (isCompatType) {
        MediaSeriesCard(
            state = state,
            onMediaClick = onMediaClick,
            onMediaLongClick = onMediaLongClick,
            modifier = modifier,
        )
    } else if (style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1 || style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X2) {
        CompactBrowseMediaCard(
            media = media,
            state = state,
            style = style,
            onMediaClick = onMediaClick,
            onMediaLongClick = onMediaLongClick,
            modifier = modifier,
        )
    } else {
        ExpandedBrowseMediaCard(
            media = media,
            state = state,
            onMediaClick = onMediaClick,
            onMediaLongClick = onMediaLongClick,
            modifier = modifier,
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ExpandedBrowseMediaCard(
    media: MediaBase,
    state: MediaSeriesCardUiState,
    onMediaClick: () -> Unit,
    onMediaLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isAnime = media.type == KeyUtil.ANIME

    Card(
        modifier = modifier
            .padding(dimensionResource(R.dimen.lg_margin))
            .combinedClickable(onClick = onMediaClick, onLongClick = onMediaLongClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(context.getCompatColorAttr(R.attr.cardColor))),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Column {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f / KeyUtil.AspectRatio),
                ) {
                    AniTrendImage(
                        model = state.coverImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                AndroidView(
                    factory = { ratingContext -> RatingTextView(ratingContext).apply { id = R.id.custom_rating_widget } },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (isAnime) dimensionResource(R.dimen.md_margin) else dimensionResource(R.dimen.lg_margin))
                        .testTag(MEDIA_BROWSE_RATING_TAG),
                    update = { ratingView -> ratingView.setAverageRating(media) },
                    onRelease = { ratingView -> ratingView.onViewRecycled() },
                )
            }
            AndroidView(
                factory = { statusContext -> SeriesStatusWidget(statusContext) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.sm_margin))
                    .testTag(MEDIA_BROWSE_STATUS_TAG),
                update = { statusView -> SeriesStatusWidget.setStatus(statusView, media) },
            )
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin))) {
                if (isAnime) {
                    AiringCaption(media = media)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                }
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = state.yearTypeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CompactBrowseMediaCard(
    media: MediaBase,
    state: MediaSeriesCardUiState,
    style: Int,
    onMediaClick: () -> Unit,
    onMediaLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isAnime = media.type == KeyUtil.ANIME
    val hideRating = style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X2

    Card(
        modifier = modifier
            .padding(dimensionResource(R.dimen.lg_margin))
            .combinedClickable(onClick = onMediaClick, onLongClick = onMediaLongClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(context.getCompatColorAttr(R.attr.cardColor))),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Box {
            Row(verticalAlignment = Alignment.Bottom) {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.md_margin)))
                Box(modifier = Modifier.width(dimensionResource(R.dimen.avatar_size_lg))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f / KeyUtil.AspectRatio),
                    ) {
                        AniTrendImage(
                            model = state.coverImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(dimensionResource(R.dimen.lg_margin))) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))
                    if (isAnime) {
                        AiringCaption(media = media)
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.sm_margin)))
                    }
                    Text(
                        text = state.yearTypeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(modifier = Modifier.matchParentSize()) {
                AndroidView(
                    factory = { statusContext -> SeriesStatusWidget(statusContext) },
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.md_margin))
                        .fillMaxHeight()
                        .testTag(MEDIA_BROWSE_STATUS_TAG),
                    update = { statusView -> SeriesStatusWidget.setStatus(statusView, media) },
                )
            }
            if (!hideRating) {
                AndroidView(
                    factory = { ratingContext -> RatingTextView(ratingContext).apply { id = R.id.custom_rating_widget } },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(dimensionResource(R.dimen.md_margin))
                        .testTag(MEDIA_BROWSE_RATING_TAG),
                    update = { ratingView -> ratingView.setAverageRating(media) },
                    onRelease = { ratingView -> ratingView.onViewRecycled() },
                )
            }
        }
    }
}

@Composable
private fun AiringCaption(media: MediaBase) {
    AndroidView(
        factory = { context -> AiringTextView(context) },
        modifier = Modifier.fillMaxWidth().testTag(MEDIA_BROWSE_AIRING_TAG),
        update = { airingView -> AiringTextView.setAiring(airingView, media) },
    )
}
