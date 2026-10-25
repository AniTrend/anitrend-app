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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.markdown.rememberMarkdownLinkHandler
import com.mxt.anitrend.ui.sheet.SpoilerSheet
import io.noties.markwon.Markwon

/**
 * Review reader sheet: the review body renders through the shared
 * [MarkdownText] bridge, with author and media deep links and native Compose
 * voting via [ReviewVoteRow]. Voting dispatches the three-state
 * [ReviewRating] toggle (same target again returns to NO_VOTE) through the
 * owning route into [com.mxt.anitrend.domain.review.interactor.RateReviewInteractor].
 * [ReviewRecord.score] stays a display-only average, never a vote input.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewReaderSheet(
    review: ReviewRecord?,
    onDismiss: () -> Unit,
    onUserClick: (Long) -> Unit,
    onMediaClick: (Long, String?) -> Unit,
    onRate: (rating: ReviewRating) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon: Markwon = remember { createMarkwon(context, linkHandler) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val current = review
            if (current == null) {
                Text(
                    text = stringResource(R.string.layout_empty_response),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@ModalBottomSheet
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AniTrendImage(
                    model = current.user?.avatar,
                    contentDescription = current.user?.name,
                    modifier =
                    Modifier
                        .size(48.dp)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = current.user?.name ?: stringResource(R.string.label_unknown),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = current.summary.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = { current.user?.id?.let(onUserClick) },
                    enabled = current.user?.id != null,
                ) {
                    Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AniTrendImage(
                    model = current.media?.coverImage,
                    contentDescription = current.media?.titleUserPreferred,
                    modifier =
                    Modifier
                        .width(72.dp)
                        .aspectRatio(0.7f)
                        .clip(MaterialTheme.shapes.medium),
                )
                TextButton(
                    onClick = {
                        current.media?.id?.let { mediaId -> onMediaClick(mediaId, current.mediaType) }
                    },
                    enabled = current.media?.id != null,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = current.media?.titleUserPreferred ?: stringResource(R.string.label_unknown),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { (current.score.coerceIn(0, 100)) / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.review_rating_average, current.score),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ReviewVoteRow(
                state =
                ReviewVoteRowState(
                    upVoteCount = current.rating,
                    downVoteCount = (current.ratingAmount - current.rating).coerceAtLeast(0),
                    userRating = current.userRating,
                ),
                onUpVoteClick = { onRate(ReviewRating.UP_VOTE) },
                onDownVoteClick = { onRate(ReviewRating.DOWN_VOTE) },
                modifier = Modifier.fillMaxWidth(),
            )
            MarkdownText(
                markdown = current.body ?: stringResource(R.string.layout_empty_response),
                markwon = markwon,
                onLinkClick = linkHandler,
                onLinkLongClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}
