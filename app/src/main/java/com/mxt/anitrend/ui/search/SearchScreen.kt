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
package com.mxt.anitrend.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.character.CharacterRow
import com.mxt.anitrend.ui.designsystem.AniTrendScaffold
import com.mxt.anitrend.ui.format.mediaFormatLabelRes
import com.mxt.anitrend.ui.image.AniTrendImage
import com.mxt.anitrend.ui.media.MediaGridMinTileWidth
import com.mxt.anitrend.ui.media.MediaGridTile
import com.mxt.anitrend.ui.media.StudioRow
import com.mxt.anitrend.ui.state.EmptyState
import com.mxt.anitrend.ui.state.ErrorState
import com.mxt.anitrend.ui.state.LoadingState
import com.mxt.anitrend.ui.staff.StaffRow
import com.mxt.anitrend.ui.staff.StaffRowUiState
import com.mxt.anitrend.viewmodel.CharacterSearchViewModel
import com.mxt.anitrend.viewmodel.MediaSearchViewModel
import com.mxt.anitrend.viewmodel.StaffSearchViewModel
import com.mxt.anitrend.viewmodel.StudioSearchViewModel
import com.mxt.anitrend.viewmodel.UserSearchViewModel

/** Search entity types rendered as in-screen chips over the existing search ViewModels. */
internal enum class SearchEntity(
    val titleRes: Int,
    val isMedia: Boolean,
) {
    ANIME(R.string.label_media_type_anime, isMedia = true),
    MANGA(R.string.label_media_type_manga, isMedia = true),
    CHARACTERS(R.string.anime_page_title_characters, isMedia = false),
    STAFF(R.string.title_activity_staff, isMedia = false),
    STUDIOS(R.string.title_activity_studio, isMedia = false),
    USERS(R.string.search_entity_users, isMedia = false),
}

/**
 * Search screen: one query field plus entity-type chips (Anime, Manga,
 * Characters, Staff, Studios, Users). Chip switches are in-screen only and
 * re-query the owning ViewModel; they never add navigation entries. User follow
 * toggles route through [UserSearchViewModel.toggleFollow] with authoritative
 * state from its followStates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    mediaViewModel: MediaSearchViewModel,
    characterViewModel: CharacterSearchViewModel,
    staffViewModel: StaffSearchViewModel,
    studioViewModel: StudioSearchViewModel,
    userViewModel: UserSearchViewModel,
    onOpenMedia: (Long, String?) -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenStaff: (Long) -> Unit,
    onOpenStudio: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var entityName by rememberSaveable { mutableStateOf(SearchEntity.ANIME.name) }
    val entity = remember(entityName) { SearchEntity.valueOf(entityName) }

    val characterState by characterViewModel.state.collectAsState()
    val staffState by staffViewModel.state.collectAsState()
    val studioState by studioViewModel.state.collectAsState()
    val userState by userViewModel.state.collectAsState()
    val userFollowStates by userViewModel.followStates.collectAsState()
    val mediaItems = mediaViewModel.pagingDataFlow.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    val trimmedQuery = query.text.trim()
    LaunchedEffect(trimmedQuery, entity) {
        if (trimmedQuery.isBlank()) return@LaunchedEffect
        when (entity) {
            SearchEntity.ANIME ->
                mediaViewModel.load(
                    search = trimmedQuery,
                    type = com.mxt.anitrend.graphql.generated.MediaType.ANIME,
                    isAdult = null,
                )

            SearchEntity.MANGA ->
                mediaViewModel.load(
                    search = trimmedQuery,
                    type = com.mxt.anitrend.graphql.generated.MediaType.MANGA,
                    isAdult = null,
                )

            SearchEntity.CHARACTERS -> characterViewModel.load(trimmedQuery, 1)
            SearchEntity.STAFF -> staffViewModel.load(trimmedQuery, 1)
            SearchEntity.STUDIOS -> studioViewModel.load(trimmedQuery, 1)
            SearchEntity.USERS -> userViewModel.load(trimmedQuery, 1)
        }
    }

    val shouldLoadMore by remember(entity, listState) {
        derivedStateOf {
            (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >=
                (listState.layoutInfo.totalItemsCount - 3)
        }
    }

    AniTrendScaffold(
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { query = TextFieldValue("") }) {
                    Text(text = stringResource(R.string.Reset), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { value -> query = value },
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(SearchEntity.entries, key = { it.name }) { entry ->
                    FilterChip(
                        selected = entity == entry,
                        onClick = { entityName = entry.name },
                        label = { Text(stringResource(entry.titleRes)) },
                    )
                }
            }
            if (trimmedQuery.isBlank()) {
                EmptyState(message = stringResource(R.string.search_hint), modifier = Modifier.fillMaxSize())
            } else {
                when (entity) {
                    SearchEntity.ANIME, SearchEntity.MANGA ->
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(MediaGridMinTileWidth),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(
                                count = mediaItems.itemCount,
                                key = mediaItems.itemKey { "media:${it.id}" },
                            ) { index ->
                                val item = mediaItems[index]
                                if (item != null) {
                                    val formatLabel = stringResource(mediaFormatLabelRes(item.mediaFormat))
                                    val score = item.averageScore
                                    val subtitle = if (score != null && score > 0) "$formatLabel · $score%" else formatLabel
                                    MediaGridTile(
                                        imageUrl = item.coverImage,
                                        title = item.title,
                                        subtitle = subtitle,
                                        contentDescription = item.title,
                                        onClick = { onOpenMedia(item.id, item.mediaType) },
                                        onLongClick = { onOpenMedia(item.id, item.mediaType) },
                                    )
                                }
                            }
                        }

                    SearchEntity.CHARACTERS ->
                        when (val state = characterState) {
                            is CharacterSearchViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                            is CharacterSearchViewModel.UiState.Error ->
                                ErrorState(message = state.message, onRetry = { characterViewModel.load(trimmedQuery, 1) }, modifier = Modifier.fillMaxSize())

                            is CharacterSearchViewModel.UiState.Success ->
                                if (state.content.pageData.isEmpty()) {
                                    EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                                } else {
                                    LaunchedEffect(shouldLoadMore) {
                                        if (shouldLoadMore) characterViewModel.load(trimmedQuery, state.content.pageData.size / 21 + 1)
                                    }
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(state.content.pageData, key = { "character:${it.id}" }) { character ->
                                            CharacterRow(
                                                characterName = character.name?.fullName.orEmpty(),
                                                imageUrl = character.image?.large,
                                                isFavourite = character.isFavourite,
                                                onClick = { onOpenCharacter(character.id) },
                                            )
                                        }
                                    }
                                }
                        }

                    SearchEntity.STAFF ->
                        when (val state = staffState) {
                            is StaffSearchViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                            is StaffSearchViewModel.UiState.Error ->
                                ErrorState(message = state.message, onRetry = { staffViewModel.load(trimmedQuery, 1) }, modifier = Modifier.fillMaxSize())

                            is StaffSearchViewModel.UiState.Success ->
                                if (state.content.pageData.isEmpty()) {
                                    EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                                } else {
                                    LaunchedEffect(shouldLoadMore) {
                                        if (shouldLoadMore) staffViewModel.load(trimmedQuery, state.content.pageData.size / 21 + 1)
                                    }
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(state.content.pageData, key = { "staff:${it.id}" }) { staff ->
                                            StaffRow(
                                                state = StaffRowUiState.from(staff),
                                                onClick = { onOpenStaff(staff.id) },
                                            )
                                        }
                                    }
                                }
                        }

                    SearchEntity.STUDIOS ->
                        when (val state = studioState) {
                            is StudioSearchViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                            is StudioSearchViewModel.UiState.Error ->
                                ErrorState(message = state.message, onRetry = { studioViewModel.load(trimmedQuery, 1) }, modifier = Modifier.fillMaxSize())

                            is StudioSearchViewModel.UiState.Success ->
                                if (state.content.pageData.isEmpty()) {
                                    EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                                } else {
                                    LaunchedEffect(shouldLoadMore) {
                                        if (shouldLoadMore) studioViewModel.load(trimmedQuery, state.content.pageData.size / 21 + 1)
                                    }
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(state.content.pageData, key = { "studio:${it.id}" }) { studio ->
                                            StudioRow(
                                                studioName = studio.name.orEmpty(),
                                                isFavourite = false,
                                            )
                                        }
                                    }
                                }
                        }

                    SearchEntity.USERS ->
                        when (val state = userState) {
                            is UserSearchViewModel.UiState.Loading -> LoadingState(modifier = Modifier.fillMaxSize())
                            is UserSearchViewModel.UiState.Error ->
                                ErrorState(message = state.message, onRetry = { userViewModel.load(trimmedQuery, 1) }, modifier = Modifier.fillMaxSize())

                            is UserSearchViewModel.UiState.Success ->
                                if (state.content.pageData.isEmpty()) {
                                    EmptyState(message = stringResource(R.string.layout_empty_response), modifier = Modifier.fillMaxSize())
                                } else {
                                    LaunchedEffect(shouldLoadMore) {
                                        if (shouldLoadMore) userViewModel.load(trimmedQuery, state.content.pageData.size / 21 + 1)
                                    }
                                    LazyColumn(
                                        state = listState,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        items(state.content.pageData, key = { "user:${it.id}" }) { user ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                AniTrendImage(
                                                    model = user.avatar?.large,
                                                    contentDescription = user.name,
                                                    modifier =
                                                    Modifier
                                                        .size(48.dp)
                                                        .clip(MaterialTheme.shapes.medium),
                                                )
                                                TextButton(
                                                    onClick = { onOpenUser(user.id) },
                                                    modifier = Modifier.weight(1f),
                                                ) {
                                                    Text(
                                                        text = user.name ?: stringResource(R.string.label_unknown),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                }
                                                val isFollowing = userFollowStates[user.id]
                                                if (isFollowing != null) {
                                                    TextButton(onClick = { userViewModel.toggleFollow(user.id) }) {
                                                        Text(
                                                            text = stringResource(
                                                                if (isFollowing) {
                                                                    R.string.description_favourite_action_remove
                                                                } else {
                                                                    R.string.description_favourite_action_add
                                                                },
                                                            ),
                                                            style = MaterialTheme.typography.labelLarge,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                }
            }
        }
    }
}
