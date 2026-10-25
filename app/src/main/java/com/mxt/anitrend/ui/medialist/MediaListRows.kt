package com.mxt.anitrend.ui.medialist

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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.AiringTextView
import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.domain.model.MediaListItemRenderModel
import com.mxt.anitrend.domain.model.MediaListItemUiModel
import com.mxt.anitrend.domain.model.toRenderModel
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.MediaListProgressControl
import com.mxt.anitrend.ui.media.MediaListProgressState
import com.mxt.anitrend.util.KeyUtil

internal const val MEDIA_LIST_CARD_TAG = "media-list-card"
internal const val MEDIA_LIST_COVER_TAG = "media-list-cover"
internal const val MEDIA_LIST_STATUS_STRIP_TAG = "media-list-status-strip"

/**
 * Immutable projection for one media-list row. The metadata widgets stay on
 * their existing View implementations through interop (exact parity for the
 * airing text, year/type caption, status strip, and score formatting), while the
 * surrounding card, cover, and progress control render natively.
 */
internal data class MediaListRowUiState(
    val id: Long,
    val mediaId: Long,
    val mediaType: String?,
    val title: String,
    val coverImage: String?,
    val renderModel: MediaListItemRenderModel,
    val maxProgress: Int,
    val isProgressEnabled: Boolean,
    val isProgressLoading: Boolean,
) {
    companion object {
        fun from(model: MediaListItemUiModel): MediaListRowUiState = MediaListRowUiState(
            id = model.id,
            mediaId = model.mediaId,
            mediaType = model.mediaType,
            title = model.mediaTitle,
            coverImage = model.mediaCoverImage,
            renderModel = model.toRenderModel(),
            maxProgress = if (model.mediaType == com.mxt.anitrend.util.KeyUtil.ANIME) model.mediaEpisodes else model.mediaChapters,
            isProgressEnabled = model.canIncrement && !model.isIncrementPending && !model.isDeletePending,
            isProgressLoading = model.isIncrementPending,
        )
    }
}

/**
 * Stateless media-list card reproducing adapter_series_airing(.compact). The
 * compact styles hide the progress pill and rating overlay (x2) and add the
 * title side margin (x1), matching the retired adapter's style handling.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun MediaListSeriesCard(
    state: MediaListRowUiState,
    style: Int,
    onIncrement: () -> Unit,
    onIncrementBusy: () -> Unit,
    onIncrementLimit: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hideEpisodesAndRating = style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X2
    if (style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1 || style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X2) {
        CompactMediaCard(
            state = state,
            style = style,
            hideEpisodesAndRating = hideEpisodesAndRating,
            onIncrement = onIncrement,
            onIncrementBusy = onIncrementBusy,
            onIncrementLimit = onIncrementLimit,
            onOpenMedia = onOpenMedia,
            onOpenManage = onOpenManage,
            modifier = modifier,
        )
    } else {
        ExpandedMediaCard(
            state = state,
            hideEpisodesAndRating = hideEpisodesAndRating,
            onIncrement = onIncrement,
            onIncrementBusy = onIncrementBusy,
            onIncrementLimit = onIncrementLimit,
            onOpenMedia = onOpenMedia,
            onOpenManage = onOpenManage,
            modifier = modifier,
        )
    }
}

/** Expanded poster card reproducing adapter_series_airing.xml. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ExpandedMediaCard(
    state: MediaListRowUiState,
    hideEpisodesAndRating: Boolean,
    onIncrement: () -> Unit,
    onIncrementBusy: () -> Unit,
    onIncrementLimit: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .testTag(MEDIA_LIST_CARD_TAG)
            .padding(dimensionResource(R.dimen.lg_margin))
            .combinedClickable(onClick = onOpenMedia, onLongClick = onOpenManage),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Column {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f / KeyUtil.AspectRatio)
                        .testTag(MEDIA_LIST_COVER_TAG),
                ) {
                    AniTrendImage(
                        model = state.coverImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f / KeyUtil.AspectRatio),
                    )
                }
                if (!hideEpisodesAndRating) {
                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        MediaListProgressControl(
                            state = state.progressState(),
                            onIncrement = onIncrement,
                            onBusyClick = onIncrementBusy,
                            onLimitReached = onIncrementLimit,
                        )
                    }
                    AndroidView(
                        factory = { context ->
                            RatingTextView(context).apply { id = R.id.custom_rating_widget }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(dimensionResource(R.dimen.lg_margin)),
                        update = { ratingView -> ratingView.setAverageRating(state.renderModel) },
                        onRelease = { ratingView -> ratingView.onViewRecycled() },
                    )
                }
            }
            SeriesStatusStrip(renderModel = state.renderModel)
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin))) {
                AiringText(renderModel = state.renderModel)
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.md_margin)))
                YearTypeText(renderModel = state.renderModel)
            }
        }
    }
}

/** Compact list card reproducing adapter_series_airing_compact.xml. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CompactMediaCard(
    state: MediaListRowUiState,
    style: Int,
    hideEpisodesAndRating: Boolean,
    onIncrement: () -> Unit,
    onIncrementBusy: () -> Unit,
    onIncrementLimit: () -> Unit,
    onOpenMedia: () -> Unit,
    onOpenManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .testTag(MEDIA_LIST_CARD_TAG)
            .padding(
                horizontal = dimensionResource(R.dimen.lg_margin),
                vertical = dimensionResource(R.dimen.md_margin),
            )
            .combinedClickable(onClick = onOpenMedia, onLongClick = onOpenManage),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.sm_margin)),
    ) {
        Box {
            Row(verticalAlignment = Alignment.Bottom) {
                Box(modifier = Modifier.width(dimensionResource(R.dimen.avatar_size_lg))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f / KeyUtil.AspectRatio)
                            .testTag(MEDIA_LIST_COVER_TAG),
                    ) {
                        AniTrendImage(
                            model = state.coverImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin)).weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            end = if (style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
                                dimensionResource(R.dimen.series_title_margin)
                            } else {
                                0.dp
                            },
                        ),
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))
                    AiringText(renderModel = state.renderModel)
                    Spacer(modifier = Modifier.height(0.dp))
                    YearTypeText(renderModel = state.renderModel)
                }
            }
            // The left status stripe spans the completed row height (a bare
            // fillMaxHeight child of a wrap-content row measures to zero in an
            // unbounded grid cell), so it overlays the finished row.
            Box(modifier = Modifier.matchParentSize()) {
                AndroidView(
                    factory = { context -> SeriesStatusWidget(context) },
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.md_margin))
                        .fillMaxHeight()
                        .testTag(MEDIA_LIST_STATUS_STRIP_TAG),
                    update = { widget -> SeriesStatusWidget.setAiringStatus(widget, state.renderModel) },
                )
            }
            if (!hideEpisodesAndRating) {
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    MediaListProgressControl(
                        state = state.progressState(),
                        onIncrement = onIncrement,
                        onBusyClick = onIncrementBusy,
                        onLimitReached = onIncrementLimit,
                    )
                }
                AndroidView(
                    factory = { context ->
                        RatingTextView(context).apply { id = R.id.custom_rating_widget }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(dimensionResource(R.dimen.md_margin)),
                    update = { ratingView -> ratingView.setAverageRating(state.renderModel) },
                    onRelease = { ratingView -> ratingView.onViewRecycled() },
                )
            }
        }
    }
}

private fun MediaListRowUiState.progressState(): MediaListProgressState = MediaListProgressState(
    progress = renderModel.progress,
    maxProgress = maxProgress,
    isEnabled = isProgressEnabled,
    isLoading = isProgressLoading,
    status = renderModel.mediaStatus,
    mediaType = mediaType,
)

@Composable
private fun SeriesStatusStrip(renderModel: MediaListItemRenderModel) {
    AndroidView(
        factory = { context -> SeriesStatusWidget(context) },
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.sm_margin))
            .testTag(MEDIA_LIST_STATUS_STRIP_TAG),
        update = { widget -> SeriesStatusWidget.setAiringStatus(widget, renderModel) },
    )
}

@Composable
private fun AiringText(renderModel: MediaListItemRenderModel) {
    AndroidView(
        factory = { context -> AiringTextView(context) },
        modifier = Modifier.fillMaxWidth(),
        update = { textView -> AiringTextView.setAiring(textView, renderModel) },
    )
}

@Composable
private fun YearTypeText(renderModel: MediaListItemRenderModel) {
    AndroidView(
        factory = { context -> SeriesYearTypeTextView(context) },
        update = { textView -> SeriesYearTypeTextView.htmlText(textView, renderModel) },
    )
}

/**
 * Stateless media-list grid. The host forwards actions and restores the viewport
 * around store updates.
 */
@Composable
internal fun MediaListGrid(
    items: List<MediaListItemUiModel>,
    style: Int,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onIncrement: (MediaListItemUiModel) -> Unit,
    onIncrementBusy: () -> Unit = {},
    onIncrementLimit: () -> Unit = {},
    onOpenMedia: (MediaListItemUiModel) -> Unit,
    onOpenManage: (MediaListItemUiModel) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(columnsFor(style))),
        modifier = modifier.fillMaxWidth(),
        state = listState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> "media-list:${item.id}" },
            contentType = { _, _ -> "media-list" },
        ) { _, item ->
            MediaListSeriesCard(
                state = MediaListRowUiState.from(item),
                style = style,
                onIncrement = { onIncrement(item) },
                onIncrementBusy = onIncrementBusy,
                onIncrementLimit = onIncrementLimit,
                onOpenMedia = { onOpenMedia(item) },
                onOpenManage = { onOpenManage(item) },
            )
        }
    }
}

internal fun columnsFor(style: Int): Int = if (style == KeyUtil.LIST_VIEW_STYLE_COMPACT_X1) {
    R.integer.single_list_x1
} else {
    R.integer.grid_list_x2
}
