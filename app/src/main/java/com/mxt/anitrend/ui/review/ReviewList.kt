package com.mxt.anitrend.ui.review

import android.view.View
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.media.LOAD_MORE_VISIBLE_THRESHOLD
import com.mxt.anitrend.viewmodel.ReviewVotePendingState

@Composable
internal fun ReviewList(
    reviews: List<ReviewRecord>,
    isBanner: Boolean,
    voteStates: Map<Long, ReviewVotePendingState>,
    listState: LazyListState,
    onVote: (ReviewRecord, ReviewRating) -> Unit,
    onImageAction: (ReviewImageAction, ReviewRecord) -> Unit,
    onReviewClick: (View, ReviewRecord) -> Unit,
    onLoadMore: () -> Unit,
    loadMoreRevision: Int = 0,
) {
    ReviewListLoadMoreEffect(listState, loadMoreRevision, onLoadMore)

    LazyColumn(state = listState) {
        items(
            items = reviews,
            key = { review -> review.id },
        ) { review ->
            val readMoreView = remember(review.id) { mutableStateOf<MaterialTextView?>(null) }
            val pending = voteStates[review.id] ?: ReviewVotePendingState()
            val voteState = ReviewVoteRowState(
                upVoteCount = review.rating,
                downVoteCount = (review.ratingAmount - review.rating).coerceAtLeast(0),
                userRating = review.userRating,
                upVoteLoading = pending.upVoteLoading,
                downVoteLoading = pending.downVoteLoading,
                colorStyle = if (isBanner) R.color.white else 0,
            )
            val onReadMore: () -> Unit = {
                readMoreView.value?.let { view -> onReviewClick(view, review) }
            }
            val itemModifier = Modifier.padding(dimensionResource(R.dimen.lg_margin))

            if (isBanner) {
                ReviewBannerRow(
                    review = review,
                    voteState = voteState,
                    onUpVoteClick = { onVote(review, ReviewRating.UP_VOTE) },
                    onDownVoteClick = { onVote(review, ReviewRating.DOWN_VOTE) },
                    onImageClick = { onImageAction(ReviewImageAction.OPEN_MEDIA, review) },
                    onImageLongClick = { onImageAction(ReviewImageAction.LONG_PRESS_MEDIA, review) },
                    onReadMoreClick = onReadMore,
                    onReadMoreViewCreated = { readMoreView.value = it },
                    onReadMoreViewReleased = { view ->
                        if (readMoreView.value === view) readMoreView.value = null
                    },
                    modifier = itemModifier,
                )
            } else {
                ReviewSeriesRow(
                    review = review,
                    voteState = voteState,
                    onUpVoteClick = { onVote(review, ReviewRating.UP_VOTE) },
                    onDownVoteClick = { onVote(review, ReviewRating.DOWN_VOTE) },
                    onProfileClick = { onImageAction(ReviewImageAction.OPEN_PROFILE, review) },
                    onReadMoreClick = onReadMore,
                    onReadMoreViewCreated = { readMoreView.value = it },
                    onReadMoreViewReleased = { view ->
                        if (readMoreView.value === view) readMoreView.value = null
                    },
                    onCoverLongClick = { onImageAction(ReviewImageAction.LONG_PRESS_MEDIA, review) },
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Composable
private fun ReviewListLoadMoreEffect(
    listState: LazyListState,
    loadMoreRevision: Int,
    onLoadMore: () -> Unit,
) {
    val currentOnLoadMore = rememberUpdatedState(onLoadMore)
    LaunchedEffect(listState, loadMoreRevision) {
        snapshotFlow {
            val layout = listState.layoutInfo
            ReviewListLoadSample(
                totalItemCount = layout.totalItemsCount,
                visibleItemCount = layout.visibleItemsInfo.size,
                firstVisibleItem = layout.visibleItemsInfo.firstOrNull()?.index ?: 0,
            )
        }.collect { sample ->
            if (
                sample.totalItemCount > 0 &&
                (sample.totalItemCount - sample.visibleItemCount) <=
                (sample.firstVisibleItem + LOAD_MORE_VISIBLE_THRESHOLD)
            ) {
                currentOnLoadMore.value()
            }
        }
    }
}

private data class ReviewListLoadSample(
    val totalItemCount: Int,
    val visibleItemCount: Int,
    val firstVisibleItem: Int,
)
