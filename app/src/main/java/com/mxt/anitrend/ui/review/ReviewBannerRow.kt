@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")

package com.mxt.anitrend.ui.review

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.CustomRatingBar
import com.mxt.anitrend.domain.model.MediaSummaryRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendCondensedFontFamily
import com.mxt.anitrend.ui.designsystem.aniTrendPlatformTextStyle
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.util.KeyUtil

/** Stateless browse-review banner, retaining its View-owned rating and Read More leaves. */
@Composable
internal fun ReviewBannerRow(
    review: ReviewRecord,
    voteState: ReviewVoteRowState,
    onUpVoteClick: () -> Unit,
    onDownVoteClick: () -> Unit,
    onImageClick: () -> Unit,
    onImageLongClick: () -> Unit,
    onReadMoreClick: () -> Unit,
    onReadMoreViewCreated: (MaterialTextView) -> Unit,
    onReadMoreViewReleased: (MaterialTextView) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageDescription =
        review.media?.titleUserPreferred
            ?.takeIf(String::isNotBlank)
            ?: stringResource(R.string.drawer_title_reviews)
    val readMoreLabel = stringResource(R.string.text_read_more)
    val bannerImageUrl = review.media?.bannerImage
    val bannerImageModifier =
        Modifier
            .fillMaxWidth()
            .height(
                (
                    dimensionResource(R.dimen.app_bar_height) -
                        dimensionResource(R.dimen.sm_margin)
                    ) * KeyUtil.WideAspectRatio,
            )
            .combinedClickable(
                role = Role.Button,
                onClickLabel = imageDescription,
                onLongClickLabel = imageDescription,
                onClick = onImageClick,
                onLongClick = onImageLongClick,
            )
            .semantics(mergeDescendants = true) {
                contentDescription = imageDescription
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
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (LocalInspectionMode.current) {
                    Image(
                        painter = painterResource(R.drawable.reg_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = bannerImageModifier,
                    )
                } else {
                    AniTrendImage(
                        model = bannerImageUrl?.takeIf(String::isNotBlank) ?: R.drawable.reg_bg,
                        contentDescription = null,
                        modifier = bannerImageModifier,
                        contentScale = ContentScale.Crop,
                        crossfadeDurationMillis = 350,
                    )
                }
                Box(
                    modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, colorResource(R.color.colorGradientShadowEnd)),
                            ),
                        ),
                )

                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(dimensionResource(R.dimen.spacing_xs)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AndroidView(
                        factory = { ratingContext ->
                            CustomRatingBar(ContextThemeWrapper(ratingContext, R.style.SmallRating)).apply {
                                id = R.id.series_rating
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                )
                                CustomRatingBar.setAverageScore(this, review.score)
                            }
                        },
                        modifier = Modifier.wrapContentSize(),
                        update = { ratingView -> CustomRatingBar.setAverageScore(ratingView, review.score) },
                        onRelease = CustomRatingBar::onViewRecycled,
                    )
                    Text(
                        text = review.user?.name.orEmpty(),
                        modifier =
                        Modifier
                            .padding(start = dimensionResource(R.dimen.lg_margin))
                            .background(colorResource(R.color.black_transparent_image), RoundedCornerShape(50.dp))
                            .padding(
                                horizontal = dimensionResource(R.dimen.lg_margin),
                                vertical = dimensionResource(R.dimen.md_margin),
                            ),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(platformStyle = aniTrendPlatformTextStyle),
                    )
                }

                Row(
                    modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.spacing_xs)),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = review.media?.titleUserPreferred.orEmpty(),
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = aniTrendCondensedFontFamily,
                            platformStyle = aniTrendPlatformTextStyle,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ReviewVoteRow(
                        state = voteState,
                        onUpVoteClick = onUpVoteClick,
                        onDownVoteClick = onDownVoteClick,
                        modifier = Modifier.padding(start = dimensionResource(R.dimen.lg_margin)),
                    )
                }
            }

            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.spacing_xs)),
            ) {
                Text(
                    text = review.summary.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = aniTrendCondensedFontFamily,
                        platformStyle = aniTrendPlatformTextStyle,
                    ),
                    maxLines = 3,
                    minLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (LocalInspectionMode.current) {
                    Text(
                        text = readMoreLabel,
                        modifier = Modifier.padding(
                            top = dimensionResource(R.dimen.lg_margin),
                            bottom = dimensionResource(R.dimen.md_margin),
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(platformStyle = aniTrendPlatformTextStyle),
                    )
                } else {
                    AndroidView(
                        factory = { context ->
                            createReviewReadMoreView(context).apply {
                                setOnClickListener { onReadMoreClick() }
                                onReadMoreViewCreated(this)
                            }
                        },
                        modifier =
                        Modifier
                            .padding(top = dimensionResource(R.dimen.lg_margin))
                            .semantics(mergeDescendants = true) {
                                contentDescription = readMoreLabel
                                role = Role.Button
                                onClick(label = readMoreLabel) {
                                    onReadMoreClick()
                                    true
                                }
                            },
                        onRelease = { view ->
                            view.setOnClickListener(null)
                            onReadMoreViewReleased(view)
                        },
                    )
                }
            }
        }
    }
}

internal fun createReviewReadMoreView(context: Context): MaterialTextView {
    val view = MaterialTextView(context)
    view.id = R.id.review_read_more
    view.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )
    view.text = context.getString(R.string.text_read_more)
    view.setTextAppearance(R.style.TextAppearance_AniTrend_LabelLarge)
    view.setTextColor(context.getCompatColorAttr(R.attr.colorPrimary))
    val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.md_margin)
    view.setPadding(0, verticalPadding, 0, verticalPadding)
    view.isFocusable = true
    view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    return view
}

@Preview(name = "Browse review banner", showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun ReviewBannerRowPreview() {
    val previewReview =
        ReviewRecord(
            id = 42L,
            summary = "An engaging story with a memorable cast and excellent pacing. The finale brings the themes together with surprising emotional weight.",
            mediaType = "ANIME",
            body = "Review body",
            rating = 80,
            ratingAmount = 100,
            userRating = ReviewRating.UP_VOTE.name,
            score = 84,
            isPrivate = false,
            createdAt = 1_600_000_000L,
            user = UserSummaryRecord(id = 7L, name = "Diama", avatar = null, siteUrl = null),
            media = MediaSummaryRecord(
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
    AniTrendTheme {
        ReviewBannerRow(
            review = previewReview,
            voteState = ReviewVoteRowState(
                upVoteCount = 80,
                downVoteCount = 20,
                userRating = ReviewRating.UP_VOTE.name,
                colorStyle = R.color.white,
            ),
            onUpVoteClick = {},
            onDownVoteClick = {},
            onImageClick = {},
            onImageLongClick = {},
            onReadMoreClick = {},
            onReadMoreViewCreated = {},
            onReadMoreViewReleased = {},
        )
    }
}
