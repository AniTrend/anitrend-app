package com.mxt.anitrend.ui.navigation

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.HeaderImageView
import com.mxt.anitrend.ui.profile.AvatarIndicator
import com.mxt.anitrend.ui.profile.AvatarIndicatorState
import com.mxt.anitrend.util.KeyUtil

internal data class MainNavigationDrawerState(
    @IdRes val selectedItemId: Int = 0,
    val isAuthenticated: Boolean = false,
    val headerTitle: String? = null,
    val bannerImageUrl: String? = null,
    val avatarIndicatorState: AvatarIndicatorState = AvatarIndicatorState(),
    val showUpdateAction: Boolean = false,
    val updateBadgeText: String? = null,
)

internal const val MAIN_NAV_DRAWER_HEADER_TAG = "main-navigation-drawer-header"
internal const val MAIN_NAV_DRAWER_LIST_TAG = "main-navigation-drawer-list"

private data class DrawerDestination(
    @IdRes val id: Int,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
)

private val discoverDestinations = listOf(
    DrawerDestination(R.id.nav_anime, R.string.drawer_title_anime, R.drawable.ic_movie_white_24dp),
    DrawerDestination(R.id.nav_manga, R.string.drawer_title_manga, R.drawable.ic_library_books_white_24dp),
    DrawerDestination(R.id.nav_trending, R.string.drawer_title_trending, R.drawable.ic_bubble_chart_white_24dp),
    DrawerDestination(R.id.nav_reviews, R.string.drawer_title_reviews, R.drawable.ic_rate_review_white_24dp),
)

private val manageDestinations = listOf(
    DrawerDestination(R.id.nav_airing, R.string.drawer_title_airing, R.drawable.ic_hourglass_empty_white_24dp),
    DrawerDestination(R.id.nav_hub, R.string.drawer_title_hub, R.drawable.ic_group_work_white_24dp),
    DrawerDestination(R.id.nav_myanime, R.string.drawer_title_myanime, R.drawable.ic_live_tv_white_24dp),
    DrawerDestination(R.id.nav_mymanga, R.string.drawer_title_mymanga, R.drawable.ic_book_white_24dp),
)

private val drawerItemShape = RoundedCornerShape(topEnd = 48.dp, bottomEnd = 48.dp)
private val maximumDrawerWidth = 320.dp

@Composable
internal fun MainNavigationDrawer(
    state: MainNavigationDrawerState,
    onItemClick: (Int) -> Unit,
    onHeaderClick: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxHeight()) {
        val drawerWidth = maxWidth.coerceAtMost(maximumDrawerWidth)

        LazyColumn(
            modifier = Modifier
                .width(drawerWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(bottomEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .selectableGroup()
                .testTag(MAIN_NAV_DRAWER_LIST_TAG),
        ) {
            item(key = "drawer-header") {
                DrawerHeader(
                    state = state,
                    onHeaderClick = onHeaderClick,
                    onAvatarClick = onAvatarClick,
                )
            }
            item(key = "drawer-header-spacing") { Spacer(modifier = Modifier.height(8.dp)) }
            item(key = "discover-heading") {
                DrawerSectionHeading(R.string.nav_header_discover)
            }
            if (state.isAuthenticated) {
                item(key = R.id.nav_home_feed) {
                    DrawerNavigationItem(
                        destination = DrawerDestination(
                            R.id.nav_home_feed,
                            R.string.drawer_title_home,
                            R.drawable.ic_home_white_24dp,
                        ),
                        selected = state.selectedItemId == R.id.nav_home_feed,
                        onClick = { onItemClick(R.id.nav_home_feed) },
                    )
                }
            }
            items(discoverDestinations, key = { it.id }) { destination ->
                DrawerNavigationItem(
                    destination = destination,
                    selected = state.selectedItemId == destination.id,
                    onClick = { onItemClick(destination.id) },
                )
            }

            if (state.isAuthenticated) {
                item(key = "manage-divider") { DrawerSectionDivider() }
                item(key = "manage-heading") {
                    DrawerSectionHeading(R.string.nav_header_manage)
                }
                items(manageDestinations, key = { it.id }) { destination ->
                    DrawerNavigationItem(
                        destination = destination,
                        selected = state.selectedItemId == destination.id,
                        onClick = { onItemClick(destination.id) },
                    )
                }
            }

            item(key = "extras-divider") { DrawerSectionDivider() }
            item(key = "extras-heading") {
                DrawerSectionHeading(R.string.drawer_section_extras)
            }
            item(key = if (state.isAuthenticated) R.id.nav_sign_out else R.id.nav_sign_in) {
                DrawerActionItem(
                    title = if (state.isAuthenticated) R.string.drawer_title_sign_out else R.string.drawer_title_sign_in,
                    icon = if (state.isAuthenticated) R.drawable.ic_exit_to_app_white_24dp else R.drawable.ic_person_white_24dp,
                    onClick = {
                        onItemClick(if (state.isAuthenticated) R.id.nav_sign_out else R.id.nav_sign_in)
                    },
                )
            }
            if (state.showUpdateAction) {
                item(key = R.id.nav_check_update) {
                    DrawerActionItem(
                        title = R.string.drawer_title_update,
                        icon = R.drawable.ic_cloud_download_white_24dp,
                        trailingText = state.updateBadgeText,
                        onClick = { onItemClick(R.id.nav_check_update) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader(
    state: MainNavigationDrawerState,
    onHeaderClick: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    val headerHeight =
        (dimensionResource(R.dimen.nav_header_height) - dimensionResource(R.dimen.sm_margin)) * KeyUtil.WideAspectRatio
    val headerDescription =
        stringResource(if (state.isAuthenticated) R.string.drawer_title_profile else R.string.drawer_title_sign_in)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .clickable(role = Role.Button, onClick = onHeaderClick)
            .semantics { contentDescription = headerDescription }
            .testTag(MAIN_NAV_DRAWER_HEADER_TAG),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                HeaderImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.reg_bg)
                }
            },
            update = { imageView ->
                if (imageView.tag != state.bannerImageUrl) {
                    if (state.bannerImageUrl == null) {
                        Glide.with(imageView).clear(imageView)
                        imageView.setImageResource(R.drawable.reg_bg)
                    } else {
                        imageView.setImage(state.bannerImageUrl)
                    }
                    imageView.tag = state.bannerImageUrl
                }
            },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, colorResource(R.color.colorGradientShadowEnd)),
                    ),
                ),
        )
        AvatarIndicator(
            state = state.avatarIndicatorState,
            onAvatarClick = onAvatarClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = dimensionResource(R.dimen.xl_margin))
                .offset(y = (-12).dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = dimensionResource(R.dimen.xl_margin),
                    bottom = dimensionResource(R.dimen.xl_margin),
                ),
        ) {
            Text(
                text = state.headerTitle ?: stringResource(R.string.app_name),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.app_provider),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DrawerSectionHeading(@StringRes title: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = dimensionResource(R.dimen.xl_margin) + 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(title),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DrawerSectionDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.xl_margin) + 12.dp)
            .height(dimensionResource(R.dimen.divider_height))
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun DrawerNavigationItem(
    destination: DrawerDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = dimensionResource(R.dimen.xl_margin))
            .height(48.dp)
            .clip(drawerItemShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrawerItemIcon(destination.icon, tint = contentColor)
        Text(
            text = stringResource(destination.title),
            modifier = Modifier.weight(1f),
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DrawerActionItem(
    @StringRes title: Int,
    @DrawableRes icon: Int,
    trailingText: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = dimensionResource(R.dimen.xl_margin))
            .height(48.dp)
            .clip(drawerItemShape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrawerItemIcon(icon)
        Text(
            text = stringResource(title),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(colorResource(R.color.colorDarker), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RowScope.DrawerItemIcon(
    @DrawableRes icon: Int,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    androidx.compose.material3.Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier
            .padding(start = dimensionResource(R.dimen.xl_margin) + 12.dp, end = 12.dp)
            .size(24.dp),
        tint = tint,
    )
}
