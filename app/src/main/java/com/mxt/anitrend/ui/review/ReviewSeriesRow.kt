@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendPlatformTextStyle
import com.mxt.anitrend.ui.image.AniTrendAvatarImage
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.markdown.MarkdownText
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import io.noties.markwon.Markwon

private const val REVIEW_SERIES_COVER_TAG = "review-series-cover"

/** Stateless series-review presentation with adapter-owned actions and vote state. */
@Composable
internal fun ReviewSeriesRow(
    review: ReviewRecord,
    voteState: ReviewVoteRowState,
    onUpVoteClick: () -> Unit,
    onDownVoteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onReadMoreClick: () -> Unit,
    onCoverLongClick: () -> Unit,
    markwon: Markwon,
    onLinkClick: (String) -> Unit,
    onLinkLongClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val userName = review.user?.name.orEmpty()
    val profileDescription =
        if (userName.isNotBlank()) {
            stringResource(R.string.description_review_open_profile, userName)
        } else {
            stringResource(R.string.profile_avatar_description)
        }
    val readMoreLabel = stringResource(R.string.text_read_more)
    val coverDescription = review.media?.titleUserPreferred?.takeIf(String::isNotBlank)
    val ratingDescription = stringResource(R.string.description_review_rating, review.score * 5f / 100f)
    val coverImageUrl = review.media?.coverImage
    val coverSemantics =
        Modifier
            .pointerInput(onCoverLongClick) {
                detectTapGestures(onLongPress = { onCoverLongClick() })
            }
            .semantics(mergeDescendants = true) {
                coverDescription?.let { description ->
                    contentDescription = description
                    onLongClick(label = description) {
                        onCoverLongClick()
                        true
                    }
                }
            }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_xs)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = null,
                            indication = ripple(),
                            role = Role.Button,
                            onClick = onProfileClick,
                        ).semantics(mergeDescendants = true) {
                            contentDescription = profileDescription
                        },
                ) {
                    AniTrendAvatarImage(
                        imageUrl = review.user?.avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Column(
                    modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = dimensionResource(R.dimen.lg_margin)),
                ) {
                    Text(
                        text = userName,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall.copy(platformStyle = aniTrendPlatformTextStyle),
                    )
                    Text(
                        text = DateUtil.convertDate(review.createdAt).orEmpty(),
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.md_margin)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall.copy(platformStyle = aniTrendPlatformTextStyle),
                    )
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = dimensionResource(R.dimen.spacing_small)),
                ) {
                    Text(
                        text = review.media?.titleUserPreferred.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, platformStyle = aniTrendPlatformTextStyle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))

                    if (isPreview) {
                        Text(
                            text = review.summary ?: stringResource(R.string.layout_empty_response),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface,
                            style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                platformStyle = aniTrendPlatformTextStyle,
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        MarkdownText(
                            markdown = review.summary ?: stringResource(R.string.layout_empty_response),
                            markwon = markwon,
                            onLinkClick = onLinkClick,
                            onLinkLongClick = onLinkLongClick,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            fontStyle = FontStyle.Italic,
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.lg_margin)))

                    ReviewScoreStars(
                        score = review.score,
                        contentDescription = ratingDescription,
                    )

                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(R.dimen.spacing_small)),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = readMoreLabel,
                            modifier =
                            Modifier
                                .weight(1f)
                                .padding(vertical = dimensionResource(R.dimen.md_margin))
                                .semantics(mergeDescendants = true) {
                                    contentDescription = readMoreLabel
                                    role = Role.Button
                                    onClick(label = readMoreLabel) {
                                        onReadMoreClick()
                                        true
                                    }
                                },
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge.copy(platformStyle = aniTrendPlatformTextStyle),
                        )
                        ReviewVoteRow(
                            state = voteState,
                            onUpVoteClick = onUpVoteClick,
                            onDownVoteClick = onDownVoteClick,
                        )
                    }
                }

                Box(
                    modifier =
                    Modifier
                        .width(88.dp)
                        .aspectRatio(1f / KeyUtil.AspectRatio)
                        .then(coverSemantics)
                        .testTag(REVIEW_SERIES_COVER_TAG),
                ) {
                    if (isPreview) {
                        Image(
                            painter = painterResource(R.drawable.reg_bg),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else if (!coverImageUrl.isNullOrBlank()) {
                        AniTrendImage(
                            model = coverImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            crossfadeDurationMillis = 350,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Series review row", showBackground = true, widthDp = 360, heightDp = 520)
@Composable
private fun ReviewSeriesRowPreview() {
    val review =
        ReviewRecord(
            id = 42L,
            summary = "An engaging story with a memorable cast and excellent pacing.",
            mediaType = "ANIME",
            body = "Review body",
            rating = 80,
            ratingAmount = 100,
            userRating = ReviewRating.UP_VOTE.name,
            score = 84,
            isPrivate = false,
            createdAt = 1_600_000_000L,
            user = UserSummaryRecord(id = 7L, name = "Diama", avatar = null, siteUrl = null),
            media =
            MediaSummaryRecord(
                id = 44L,
                titleUserPreferred = "Boku no Hero Academia",
                titleRomaji = null,
                titleEnglish = null,
                titleOriginal = null,
                coverImage = null,
                bannerImage = null,
                type = "ANIME",
                format = null,
                episodes = 12,
                chapters = 0,
                volumes = 0,
                status = null,
                siteUrl = null,
            ),
            revision = 1L,
        )
    AniTrendTheme("light") {
        ReviewSeriesRow(
            review = review,
            voteState =
            ReviewVoteRowState(
                upVoteCount = 80,
                downVoteCount = 20,
                userRating = ReviewRating.UP_VOTE.name,
            ),
            onUpVoteClick = {},
            onDownVoteClick = {},
            onProfileClick = {},
            onReadMoreClick = {},
            onCoverLongClick = {},
            markwon = createMarkwon(LocalContext.current) {},
            onLinkClick = {},
        )
    }
}

private const val REVIEW_SCORE_STAR_SIZE = 16
private const val REVIEW_SCORE_STAR_COUNT = 5

/**
 * Five-star average-score indicator ported from the legacy CustomRatingBar:
 * score is a 0-100 mean rendered as 0-5 stars with fractional fill.
 */
@Composable
internal fun ReviewScoreStars(
    score: Int,
    contentDescription: String,
) {
    val starSize = REVIEW_SCORE_STAR_SIZE.dp
    val rating = (score * REVIEW_SCORE_STAR_COUNT / 100f).coerceIn(0f, REVIEW_SCORE_STAR_COUNT.toFloat())

    Row(
        modifier =
        Modifier
            .wrapContentSize()
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
            },
    ) {
        repeat(REVIEW_SCORE_STAR_COUNT) { index ->
            val fraction = (rating - index).coerceIn(0f, 1f)
            Box(modifier = Modifier.size(starSize)) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_grey_600_24dp),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
                if (fraction > 0f) {
                    Box(
                        modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(starSize * fraction)
                            .clipToBounds(),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star_yellow_700_24dp),
                            contentDescription = null,
                            modifier = Modifier.size(starSize),
                        )
                    }
                }
            }
        }
    }
}
