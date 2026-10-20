package com.mxt.anitrend.ui.review

import androidx.annotation.ColorRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendCondensedBoldFontFamily
import java.util.Locale

internal data class ReviewVoteRowState(
    val upVoteCount: Int = 0,
    val downVoteCount: Int = 0,
    val userRating: String? = null,
    val upVoteLoading: Boolean = false,
    val downVoteLoading: Boolean = false,
    @ColorRes val colorStyle: Int = 0,
)

@Composable
internal fun ReviewVoteRow(
    state: ReviewVoteRowState,
    onUpVoteClick: () -> Unit,
    onDownVoteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val upVoteDescription = stringResource(R.string.description_review_vote_up)
    val downVoteDescription = stringResource(R.string.description_review_vote_down)
    val loadingDescription = stringResource(R.string.busy_please_wait)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReviewVoteAction(
            count = state.upVoteCount,
            contentDescription = upVoteDescription,
            isSelected = state.userRating == ReviewRating.UP_VOTE.name,
            isLoading = state.upVoteLoading,
            colorStyle = state.colorStyle,
            icon = R.drawable.ic_thumb_up_grey_600_18dp,
            activeIconTint = R.color.colorStateGreen,
            loadingDescription = loadingDescription,
            onClick = onUpVoteClick,
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_xs)))
        ReviewVoteAction(
            count = state.downVoteCount,
            contentDescription = downVoteDescription,
            isSelected = state.userRating == ReviewRating.DOWN_VOTE.name,
            isLoading = state.downVoteLoading,
            colorStyle = state.colorStyle,
            icon = R.drawable.ic_thumb_down_grey_600_18dp,
            activeIconTint = R.color.colorStateOrange,
            loadingDescription = loadingDescription,
            onClick = onDownVoteClick,
        )
    }
}

@Composable
private fun ReviewVoteAction(
    count: Int,
    contentDescription: String,
    isSelected: Boolean,
    isLoading: Boolean,
    @ColorRes colorStyle: Int,
    icon: Int,
    @ColorRes activeIconTint: Int,
    loadingDescription: String,
    onClick: () -> Unit,
) {
    val textColor = if (colorStyle == 0) MaterialTheme.colorScheme.onSurface else colorResource(colorStyle)
    val inactiveIconTint = if (colorStyle == 0) colorResource(R.color.colorGrey600) else textColor

    Box(
        modifier =
        Modifier
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                onClick = onClick,
            ).padding(dimensionResource(R.dimen.lg_margin))
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                selected = isSelected
            },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 200),
                ) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 200),
                    )
            },
            label = "review vote content",
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.widget_spinner_compact_size))
                        .semantics {
                            this.contentDescription = loadingDescription
                        },
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.widget_spinner_compact_size)),
                        tint =
                        if (isSelected) {
                            colorResource(activeIconTint)
                        } else {
                            inactiveIconTint
                        },
                    )
                    Text(
                        text = String.format(Locale.getDefault(), " %d ", count),
                        color = textColor,
                        style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = aniTrendCondensedBoldFontFamily,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(name = "Review vote row", showBackground = true, widthDp = 240, heightDp = 48)
@Composable
private fun ReviewVoteRowPreview() {
    AniTrendTheme("light") {
        ReviewVoteRow(
            state =
            ReviewVoteRowState(
                upVoteCount = 80,
                downVoteCount = 20,
                userRating = ReviewRating.UP_VOTE.name,
            ),
            onUpVoteClick = {},
            onDownVoteClick = {},
        )
    }
}
