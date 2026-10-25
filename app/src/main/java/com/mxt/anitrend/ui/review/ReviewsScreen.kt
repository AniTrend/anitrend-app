/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.markdown.rememberMarkdownLinkHandler
import com.mxt.anitrend.ui.sheet.SpoilerSheet
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.viewmodel.BrowseReviewViewModel
/** Review media-type chips rendered in-screen, never as separate destinations. */
internal enum class ReviewsMediaType(
    val titleRes: Int,
    val wire: String,
) {
    ANIME(R.string.label_media_type_anime, "ANIME"),
    MANGA(R.string.label_media_type_manga, "MANGA"),
}

/**
 * Reviews screen: one renderer over [BrowseReviewViewModel] with Anime/Manga
 * type chips configuring the current query; each row opens the shared
 * [ReviewReaderSheet] via [onOpenReview].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewsScreen(
    viewModel: BrowseReviewViewModel,
    onOpenReview: (ReviewRecord) -> Unit,
    onOpenUser: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var mediaTypeName by rememberSaveable { mutableStateOf(ReviewsMediaType.ANIME.name) }
    val mediaType = remember(mediaTypeName) { ReviewsMediaType.valueOf(mediaTypeName) }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { createMarkwon(context, linkHandler) }
    fun loadPage(page: Int) {
        viewModel.load(
            type = com.mxt.anitrend.graphql.generated.MediaType.valueOf(mediaType.wire),
            page = page,
            sort = null,
        )
    }

    LaunchedEffect(mediaType) {
        loadPage(1)
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackClick) {
                    Text(text = stringResource(R.string.action_back), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ReviewsMediaType.entries, key = { it.name }) { entry ->
                    FilterChip(
                        selected = mediaType == entry,
                        onClick = { mediaTypeName = entry.name },
                        label = { Text(stringResource(entry.titleRes)) },
                    )
                }
            }
            when (val current = state) {
                is BrowseReviewViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                is BrowseReviewViewModel.UiState.Error -> {
                    ErrorState(
                        message = current.message,
                        onRetry = { loadPage(1) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is BrowseReviewViewModel.UiState.Success -> {
                    if (current.content.pageData.isEmpty()) {
                        EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                    } else {
                        val shouldLoadMore by remember(current.content.pageData.size) {
                            derivedStateOf {
                                (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >=
                                    current.content.pageData.size - 3
                            }
                        }
                        LaunchedEffect(shouldLoadMore, current.content.pageData.size) {
                            if (shouldLoadMore) {
                                loadPage(current.content.pageData.size / 21 + 1)
                            }
                        }
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(current.content.pageData, key = { "review:${it.id}" }) { review ->
                                ReviewSeriesRow(
                                    review = review,
                                    voteState = ReviewVoteRowState(
                                        upVoteCount = review.rating,
                                        downVoteCount = (review.ratingAmount - review.rating).coerceAtLeast(0),
                                        userRating = review.userRating,
                                    ),
                                    markwon = markwon,
                                    onLinkClick = linkHandler,
                                    onProfileClick = { review.user?.id?.let(onOpenUser) },
                                    onReadMoreClick = { onOpenReview(review) },
                                    onCoverLongClick = {},
                                    onUpVoteClick = { viewModel.rateReview(review.id, ReviewRating.UP_VOTE) },
                                    onDownVoteClick = { viewModel.rateReview(review.id, ReviewRating.DOWN_VOTE) },
                                    onLinkLongClick = {},
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}
