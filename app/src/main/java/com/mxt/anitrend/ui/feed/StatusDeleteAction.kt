package com.mxt.anitrend.ui.feed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

@Composable
internal fun StatusDeleteAction(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deleteDescription = stringResource(R.string.dialog_title_delete_activity)
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
            )
            .padding(dimensionResource(R.dimen.widget_flipper_padding))
            .semantics(mergeDescendants = true) {
                contentDescription = deleteDescription
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
            label = "delete action content",
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
                    painter = painterResource(R.drawable.ic_delete_red_600_18dp),
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(R.dimen.widget_spinner_compact_size)),
                    tint = Color.Unspecified,
                )
            }
        }
    }
}

@Preview(name = "Delete action states", showBackground = true)
@Composable
private fun StatusDeleteActionPreview() {
    AniTrendTheme("light") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusDeleteAction(isEnabled = true, isLoading = false, onClick = {})
            StatusDeleteAction(isEnabled = false, isLoading = true, onClick = {})
        }
    }
}
