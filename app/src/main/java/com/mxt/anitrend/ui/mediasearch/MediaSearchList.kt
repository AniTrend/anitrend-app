package com.mxt.anitrend.ui.mediasearch

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.paging.LoadStateFooter
import com.mxt.anitrend.ui.paging.LoadStateFooterUiState
import com.mxt.anitrend.base.custom.view.text.RatingTextView
import com.mxt.anitrend.base.custom.view.text.SeriesYearTypeTextView
import com.mxt.anitrend.base.custom.view.widget.SeriesStatusWidget
import com.mxt.anitrend.binding.setAverageRating
import com.mxt.anitrend.domain.model.MediaSearchItemRenderModel
import com.mxt.anitrend.domain.model.MediaSearchItemUiModel
import com.mxt.anitrend.domain.model.toRenderModel
import com.mxt.anitrend.domain.model.RecommendationItemUiModel
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import java.util.Locale

internal const val MEDIA_SERIES_CARD_TAG = "media-series-card"
internal const val MEDIA_SERIES_COVER_TAG = "media-series-cover"
internal const val MEDIA_SERIES_RATING_TAG = "media-series-rating"
internal const val MEDIA_SERIES_STATUS_TAG = "media-series-status"

/**
 * Immutable projection reproducing the retired adapter_series bindings: cover,
 * status strip, title, year/type caption, and the rating/list-status overlay.
 */
internal data class MediaSeriesCardUiState(
    val id: Long,
    val title: String,
    val coverImage: String?,
    val statusColorResource: Int,
    val yearTypeText: String,
    val renderModel: MediaSearchItemRenderModel,
    val hasMediaListEntry: Boolean,
    val mediaListStatus: String?,
) {
    companion object {
        fun from(
            context: Context,
            model: MediaSearchItemUiModel,
        ): MediaSeriesCardUiState {
            val renderModel = model.toRenderModel()
            return MediaSeriesCardUiState(
                id = model.id,
                title = model.title,
                coverImage = model.coverImage,
                statusColorResource = statusColorResource(model.mediaStatus),
                yearTypeText = yearTypeText(context, renderModel),
                renderModel = renderModel,
                hasMediaListEntry = false,
                mediaListStatus = null,
            )
        }

        fun from(context: Context, model: RecommendationItemUiModel): MediaSeriesCardUiState {
            val renderModel = MediaSearchItemRenderModel(
                mediaStartDate = model.mediaStartDate,
                mediaType = model.mediaType,
                mediaFormat = model.mediaFormat,
                mediaEpisodes = model.mediaEpisodes,
                mediaChapters = model.mediaChapters,
                averageScore = model.averageScore,
                isFavourite = model.isFavourite,
            )
            return MediaSeriesCardUiState(
                id = model.mediaId,
                title = model.title,
                coverImage = model.coverImage,
                statusColorResource = statusColorResource(model.mediaStatus),
                yearTypeText = yearTypeText(context, renderModel),
                renderModel = renderModel,
                hasMediaListEntry = false,
                mediaListStatus = null,
            )
        }

        /**
         * Reuses the search card's adapter-series fields for legacy media rows.
         * Year/type text comes from the same helper used by the XML-bound card.
         */
        fun from(
            context: Context,
            model: MediaBase,
        ): MediaSeriesCardUiState {
            val renderModel = MediaSearchItemRenderModel(
                mediaStartDate = null,
                mediaType = model.type,
                mediaFormat = model.format,
                mediaEpisodes = model.episodes,
                mediaChapters = model.chapters,
                averageScore = model.averageScore,
                isFavourite = model.isFavourite,
            )
            return MediaSeriesCardUiState(
                id = model.id,
                title = model.title?.userPreferred.orEmpty(),
                coverImage = model.coverImage?.extraLarge ?: model.coverImage?.large,
                statusColorResource = statusColorResource(model.status),
                yearTypeText = SeriesYearTypeTextView.formatText(context, model).orEmpty(),
                renderModel = renderModel,
                hasMediaListEntry = model.mediaListEntry != null,
                mediaListStatus = model.mediaListEntry?.status,
            )
        }

        private fun statusColorResource(mediaStatus: String?): Int = SeriesStatusWidget.statusColorResource(mediaStatus)

        private fun yearTypeText(
            context: Context,
            model: MediaSearchItemRenderModel,
        ): String {
            val startDate = model.mediaStartDate
            val year =
                if (startDate?.isValidDate == true) {
                    String.format(Locale.getDefault(), "%d", startDate.year ?: 0)
                } else {
                    context.getString(R.string.tba_placeholder)
                }
            val typeText =
                when (model.mediaType) {
                    KeyUtil.ANIME ->
                        if (CompatUtil.equals(model.mediaFormat, KeyUtil.MOVIE)) {
                            CompatUtil.capitalizeWords(model.mediaFormat)
                        } else if (model.mediaEpisodes > 0) {
                            context.getString(R.string.text_anime_episodes, model.mediaEpisodes)
                        } else {
                            CompatUtil.capitalizeWords(model.mediaFormat)
                        }
                    KeyUtil.MANGA ->
                        if (model.mediaChapters > 0) {
                            context.getString(R.string.text_manga_chapters, model.mediaChapters)
                        } else {
                            CompatUtil.capitalizeWords(model.mediaFormat)
                        }
                    else -> return ""
                }
            return String.format(Locale.getDefault(), "%s - %s", year, typeText)
        }
    }
}

/**
 * Stateless series card reproducing adapter_series.xml: aspect cover, coloured
 * status strip, title, year/type caption, and the RatingTextView rating/favourite
 * and list-status overlay kept through interop for score-format parity.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun MediaSeriesCard(
    state: MediaSeriesCardUiState,
    onMediaClick: () -> Unit,
    onMediaLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .testTag(MEDIA_SERIES_CARD_TAG)
            .padding(dimensionResource(R.dimen.lg_margin))
            .combinedClickable(onClick = onMediaClick, onLongClick = onMediaLongClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
        colors =
        CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(
                context.getCompatColorAttr(R.attr.cardColor),
            ),
        ),
        elevation =
        CardDefaults.cardElevation(
            defaultElevation = dimensionResource(R.dimen.sm_margin),
        ),
    ) {
        Box {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f / KeyUtil.AspectRatio)
                        .testTag(MEDIA_SERIES_COVER_TAG),
                ) {
                    AniTrendImage(
                        model = state.coverImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.sm_margin))
                        .background(colorResource(state.statusColorResource))
                        .testTag(MEDIA_SERIES_STATUS_TAG),
                )
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin))) {
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
            AndroidView(
                factory = { ratingContext ->
                    RatingTextView(ratingContext).apply { id = R.id.custom_rating_widget }
                },
                modifier = Modifier
                    .padding(
                        start = dimensionResource(R.dimen.lg_margin),
                        top = dimensionResource(R.dimen.lg_margin),
                    )
                    .testTag(MEDIA_SERIES_RATING_TAG),
                update = { ratingView ->
                    ratingView.setAverageRating(state.renderModel)
                    ratingView.setListStatus(state.hasMediaListEntry, state.mediaListStatus)
                },
                onRelease = { ratingView -> ratingView.onViewRecycled() },
            )
        }
    }
}

/**
 * Stateless media-search list host replacing the Paging3 MediaSearchAdapter. The
 * fragment owns query submission, refresh requests, and all navigation.
 */
@Composable
internal fun MediaSearchPagingList(
    lazyPagingItems: LazyPagingItems<MediaSearchItemUiModel>,
    modifier: Modifier = Modifier,
    listState: LazyGridState = rememberLazyGridState(),
    onMediaClick: (MediaSearchItemUiModel) -> Unit,
    onMediaLongClick: (MediaSearchItemUiModel) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_giphy_x3)),
        modifier = modifier.fillMaxSize(),
        state = listState,
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> "media:${lazyPagingItems.peek(index)?.id ?: index}" },
            contentType = { "media-series" },
        ) { index ->
            val item = lazyPagingItems[index]
            if (item != null) {
                val context = LocalContext.current
                MediaSeriesCard(
                    state = MediaSeriesCardUiState.from(context, item),
                    onMediaClick = { onMediaClick(item) },
                    onMediaLongClick = { onMediaLongClick(item) },
                )
            }
        }
        val appendState = lazyPagingItems.loadState.append
        if (appendState is LoadState.Loading || appendState is LoadState.Error) {
            item(key = "media-search-load-state") {
                LoadStateFooter(
                    state = LoadStateFooterUiState.from(appendState),
                    onRetry = { lazyPagingItems.retry() },
                )
            }
        }
    }
}
