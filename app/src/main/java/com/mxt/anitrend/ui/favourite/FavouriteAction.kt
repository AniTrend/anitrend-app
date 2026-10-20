package com.mxt.anitrend.ui.favourite

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendCondensedBoldFontFamily
import java.util.Locale

internal data class FavouriteActionState(
    val count: Int,
    val isLiked: Boolean,
    val isEnabled: Boolean,
    val isLoading: Boolean,
)

@Composable
internal fun FavouriteAction(
    state: FavouriteActionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionDescription =
        stringResource(
            if (state.isLiked) {
                R.string.description_favourite_action_remove
            } else {
                R.string.description_favourite_action_add
            },
        )
    val loadingDescription = stringResource(R.string.busy_please_wait)
    val textColor =
        if (state.isEnabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        }

    Box(
        modifier =
        modifier
            .clickable(
                interactionSource = null,
                indication = ripple(),
                enabled = state.isEnabled,
                role = Role.Button,
                onClick = onClick,
            ).padding(dimensionResource(R.dimen.md_margin))
            .semantics(mergeDescendants = true) {
                this.contentDescription = actionDescription
                selected = state.isLiked
            },
        contentAlignment = Alignment.Center,
    ) {
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
            label = "favorite action content",
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
                        painter = painterResource(R.drawable.ic_favorite_grey_600_18dp),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.widget_spinner_compact_size)),
                        tint =
                        if (state.isLiked) {
                            colorResource(R.color.colorStateRed)
                        } else {
                            colorResource(R.color.colorGrey600)
                        },
                    )
                    Text(
                        text = String.format(Locale.getDefault(), " %d ", state.count),
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

@Preview(name = "Favorite action", showBackground = true, widthDp = 120, heightDp = 48)
@Composable
private fun FavouriteActionPreview() {
    AniTrendTheme("light") {
        FavouriteAction(
            state = FavouriteActionState(count = 5, isLiked = true, isEnabled = true, isLoading = false),
            onClick = {},
        )
    }
}
