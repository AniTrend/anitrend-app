/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.markdown.MarkdownText

/** Profile surfaces with complete See all routes. */
enum class ProfileSection { ABOUT, ANIME, MANGA, REVIEWS, FOLLOWERS, FOLLOWING }

/**
 * Profile screen: identity header, follow counters, statistics row and the
 * shared section tabs, with each user surface routed to its complete paged
 * screen. Task surfaces (likes, follows) open as sheets owned by the route.
 */
@Composable
internal fun ProfileScreen(
    avatar: AvatarIndicatorState,
    displayName: String,
    about: String?,
    stats: UserStatisticsRecord?,
    aboutStats: AboutPanelStats,
    selectedTabIndex: Int,
    markwon: io.noties.markwon.Markwon,
    onLinkClick: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onAnimeTotalClick: () -> Unit,
    onAnimeTimeClick: (String) -> Unit,
    onMangaTotalClick: () -> Unit,
    onMangaChaptersClick: (String) -> Unit,
    onSeeAll: (ProfileSection) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackClick) {
                    Text(text = stringResource(R.string.action_back), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item(key = "identity") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AvatarIndicator(state = avatar, onAvatarClick = onAvatarClick)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        AboutPanelStatsRow(
                            stats = aboutStats,
                            onFollowingClick = { onSeeAll(ProfileSection.FOLLOWING) },
                            onFollowersClick = { onSeeAll(ProfileSection.FOLLOWERS) },
                            onFavouritesClick = { onSeeAll(ProfileSection.ABOUT) },
                        )
                    }
                }
            }
            item(key = "stats") {
                SectionHeader(title = stringResource(R.string.profile_section_overview), onSeeAll = { onSeeAll(ProfileSection.ABOUT) })
                ProfileStatsRow(
                    stats = stats,
                    onAnimeTotalClick = onAnimeTotalClick,
                    onAnimeTimeClick = onAnimeTimeClick,
                    onMangaTotalClick = onMangaTotalClick,
                    onMangaChaptersClick = onMangaChaptersClick,
                )
            }
            item(key = "about") {
                SectionHeader(title = stringResource(R.string.anime_page_title_overview), onSeeAll = { onSeeAll(ProfileSection.ABOUT) })
                MarkdownText(
                    markdown = about ?: stringResource(R.string.layout_empty_response),
                    markwon = markwon,
                    onLinkClick = onLinkClick,
                    onLinkLongClick = {},
                )
            }
            item(key = "tabs") {
                ProfileSectionTabs(selectedTabIndex = selectedTabIndex, onTabSelected = onTabSelected)
            }
            item(key = "user-surfaces") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SurfaceLink(label = stringResource(R.string.anime), onClick = { onSeeAll(ProfileSection.ANIME) })
                    SurfaceLink(label = stringResource(R.string.manga), onClick = { onSeeAll(ProfileSection.MANGA) })
                    SurfaceLink(label = stringResource(R.string.drawer_title_reviews), onClick = { onSeeAll(ProfileSection.REVIEWS) })
                    SurfaceLink(label = stringResource(R.string.title_favourites_list), onClick = { onSeeAll(ProfileSection.ABOUT) })
                }
            }
        }
    }
}

@Composable
private fun SurfaceLink(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = onClick) {
            Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(onClick = onSeeAll) {
            Text(text = stringResource(R.string.action_see_all), style = MaterialTheme.typography.labelLarge)
        }
    }
}
