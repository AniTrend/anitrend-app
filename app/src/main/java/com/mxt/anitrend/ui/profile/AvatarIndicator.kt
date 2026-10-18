package com.mxt.anitrend.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendAvatarImage
import com.mxt.anitrend.ui.image.AniTrendImage

internal data class AvatarIndicatorState(
    val avatarUrl: String? = null,
    val hasRenderedAvatar: Boolean = false,
    val unreadNotificationCount: Int = 0,
    val showNotificationIndicator: Boolean = false,
)

private const val AVATAR_IMAGE_TAG = "avatar-indicator-image"

private val unreadCountTextStyle =
    TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = true),
    )

@Composable
internal fun AvatarIndicator(
    state: AvatarIndicatorState,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatarDescription = stringResource(R.string.profile_avatar_description)
    Box(modifier = modifier.size(dimensionResource(R.dimen.avatar_size))) {
        AvatarImage(
            avatarUrl = state.avatarUrl,
            hasRenderedAvatar = state.hasRenderedAvatar,
            modifier =
            Modifier
                .fillMaxSize()
                .testTag(AVATAR_IMAGE_TAG),
        )
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = null,
                    indication = null,
                    role = Role.Button,
                    onClick = onAvatarClick,
                )
                .semantics {
                    contentDescription = avatarDescription
                },
        )
        if (state.showNotificationIndicator && state.unreadNotificationCount > 0) {
            Box(
                modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.unreadNotificationCount.toString(),
                    // Match the TextView glyph position in the XML baseline capture.
                    modifier = Modifier.absoluteOffset(x = (-0.5).dp, y = (-0.5).dp),
                    style = unreadCountTextStyle,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun AvatarImage(
    avatarUrl: String?,
    hasRenderedAvatar: Boolean,
    modifier: Modifier,
) {
    if (hasRenderedAvatar) {
        AniTrendAvatarImage(
            imageUrl = avatarUrl,
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        AniTrendImage(
            model = R.mipmap.ic_launcher_round,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            crossfadeDurationMillis = 0,
        )
    }
}

@Preview(name = "Avatar with unread count", showBackground = true, widthDp = 64, heightDp = 64)
@Composable
private fun AvatarIndicatorPreview() {
    AniTrendTheme {
        AvatarIndicator(
            state =
            AvatarIndicatorState(
                hasRenderedAvatar = false,
                unreadNotificationCount = 2,
                showNotificationIndicator = true,
            ),
            onAvatarClick = {},
        )
    }
}
