@file:Suppress("UnstableApiUsage")

package com.mxt.anitrend.ui.user

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.FollowStateWidget
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.image.AniTrendAvatarImage

private const val USER_ROW_TAG = "user-row-card"
private const val USER_ROW_AVATAR_TAG = "user-row-avatar"
private const val USER_ROW_FOLLOW_TAG = "user-row-follow"

/**
 * Display fields are immutable snapshots because UserBase equality compares only IDs.
 * The target entity supplies identity, not the follow control's display state.
 */
internal data class UserRowUiState(
    val model: UserBase,
    val name: String?,
    val avatar: ImageBase?,
    // Canonical committed flag, independent of the legacy entity's mutable flag.
    val isFollowing: Boolean,
)

/** Renders one user row while its owner retains follow and navigation state. */
@Composable
internal fun UserRow(
    state: UserRowUiState,
    currentUser: UserBase?,
    followListener: FollowStateWidget.Listener,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.cardColor)) }
    val typography = MaterialTheme.typography
    val nameStyle = remember(typography) {
        typography.bodyMedium.copy(platformStyle = PlatformTextStyle(includeFontPadding = true))
    }
    val avatar = state.avatar

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(cardColor)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(USER_ROW_TAG)
            .padding(dimensionResource(R.dimen.lg_margin)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AniTrendAvatarImage(
            imageUrl = avatar?.large,
            contentDescription = null,
            modifier =
            Modifier
                .size(dimensionResource(R.dimen.avatar_size_md))
                .testTag(USER_ROW_AVATAR_TAG),
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.xl_margin)))
        Text(
            text = state.name.orEmpty(),
            modifier = Modifier.weight(1f),
            style = nameStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.xl_margin)))
        AndroidView(
            factory = { followContext ->
                FollowStateWidget(followContext).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                }
            },
            modifier = Modifier.wrapContentSize().testTag(USER_ROW_FOLLOW_TAG),
            update = { followWidget ->
                followWidget.setCurrentUser(currentUser)
                followWidget.setUserState(state.model.id, state.isFollowing)
                followWidget.setListener(followListener)
            },
            onRelease = { followWidget ->
                followWidget.setListener(null)
                followWidget.onViewRecycled()
            },
        )
    }
}

@Preview(name = "User row", showBackground = true, widthDp = 360)
@Composable
private fun UserRowPreview() {
    val currentUser = remember { UserBase(name = "current-user").apply { id = 1L } }
    val model = remember { UserBase(name = "Compose user").apply { id = 2L } }
    val state = remember(model) {
        UserRowUiState(model = model, name = model.name, avatar = model.avatar, isFollowing = model.isFollowing)
    }
    val followListener = remember { FollowStateWidget.Listener { _ -> } }
    AniTrendTheme {
        UserRow(
            state = state,
            currentUser = currentUser,
            followListener = followListener,
            onClick = {},
        )
    }
}
