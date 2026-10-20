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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun FavouriteToolbarAction(
    isFavourite: Boolean,
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionDescription =
        stringResource(
            if (isFavourite) {
                R.string.description_favourite_action_remove
            } else {
                R.string.description_favourite_action_add
            },
        )
    val loadingDescription = stringResource(R.string.busy_please_wait)

    Box(
        modifier =
        modifier
            .clickable(
                interactionSource = null,
                indication = ripple(),
                role = Role.Button,
                enabled = isEnabled,
                onClick = onClick,
            ).padding(dimensionResource(R.dimen.mg_margin))
            .semantics(mergeDescendants = true) {
                contentDescription = actionDescription
                selected = isFavourite
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
            label = "toolbar favorite action content",
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.widget_spinner_compact_size))
                        .semantics {
                            contentDescription = loadingDescription
                        },
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    painter =
                    painterResource(
                        if (isFavourite) {
                            R.drawable.ic_favorite_white_24dp
                        } else {
                            R.drawable.ic_favorite_border_white_24dp
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.avatar_ripple_radius)),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(name = "Toolbar favorite actions", showBackground = true)
@Composable
private fun FavouriteToolbarActionPreview() {
    AniTrendTheme("light") {
        Row {
            FavouriteToolbarAction(isFavourite = false, isEnabled = true, isLoading = false, onClick = {})
            FavouriteToolbarAction(isFavourite = true, isEnabled = true, isLoading = false, onClick = {})
            FavouriteToolbarAction(isFavourite = true, isEnabled = true, isLoading = true, onClick = {})
        }
    }
}
