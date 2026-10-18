package com.mxt.anitrend.ui.media

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.KeyUtil

internal const val MEDIA_LIST_PROGRESS_CONTROL_TAG = "media-list-progress-control"

internal data class MediaListProgressState(
    val progress: Int,
    val maxProgress: Int,
    val isEnabled: Boolean,
    val isLoading: Boolean,
    val status: String?,
    val mediaType: String?,
)

@Composable
internal fun MediaListProgressControl(
    state: MediaListProgressState,
    onIncrement: () -> Unit,
    onBusyClick: () -> Unit,
    onLimitReached: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tbaLabel = stringResource(R.string.TBA)
    val busyDescription = stringResource(R.string.busy_please_wait)
    val progressLabel =
        remember(state.progress, state.maxProgress, state.status, tbaLabel) {
            if (state.status == KeyUtil.NOT_YET_RELEASED) {
                tbaLabel
            } else {
                val total = if (state.maxProgress > 0) state.maxProgress.toString() else "?"
                val label = "${state.progress}/$total"
                if (state.isIncrementLimitReached()) label else "$label +"
            }
        }
    val typography = MaterialTheme.typography
    val progressTextStyle =
        remember(typography) {
            typography.bodyMedium.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = TextUnit.Unspecified,
                platformStyle = PlatformTextStyle(includeFontPadding = true),
            )
        }

    Box(
        modifier =
        modifier
            .padding(dimensionResource(R.dimen.md_margin))
            .clip(RoundedCornerShape(dimensionResource(R.dimen.xl_margin)))
            .background(colorResource(R.color.colorTransparent))
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                onClick = {
                    when {
                        state.isLoading -> onBusyClick()
                        state.isEnabled -> onIncrement()
                        state.isIncrementLimitReached() -> onLimitReached()
                    }
                },
            ).testTag(MEDIA_LIST_PROGRESS_CONTROL_TAG),
        contentAlignment = Alignment.Center,
    ) {
        if (state.isLoading) {
            // Keep the control at its label size while the spinner is visible.
            Text(
                text = progressLabel,
                modifier =
                Modifier
                    .padding(dimensionResource(R.dimen.lg_margin))
                    .alpha(0f)
                    .clearAndSetSemantics {},
                style = progressTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
            )
        }
        AnimatedContent(
            targetState = state.isLoading,
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
            label = "media list progress content",
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.mg_margin))
                        .semantics {
                            contentDescription = busyDescription
                        },
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = progressLabel,
                    modifier = Modifier.padding(dimensionResource(R.dimen.lg_margin)),
                    style = progressTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
        }
    }
}

@Preview(name = "Media list progress control", showBackground = true, widthDp = 80, heightDp = 48)
@Composable
private fun MediaListProgressControlPreview() {
    AniTrendTheme {
        Box(
            modifier =
            Modifier
                .size(width = 80.dp, height = 48.dp)
                .background(Color.DarkGray),
        ) {
            MediaListProgressControl(
                state =
                MediaListProgressState(
                    progress = 3,
                    maxProgress = 12,
                    isEnabled = true,
                    isLoading = false,
                    status = "RELEASING",
                    mediaType = KeyUtil.ANIME,
                ),
                onIncrement = {},
                onBusyClick = {},
                onLimitReached = {},
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

private fun MediaListProgressState.isIncrementLimitReached(): Boolean = maxProgress > 0 && progress >= maxProgress
