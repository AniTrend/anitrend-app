package com.mxt.anitrend.ui.favourite

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.character.CharacterGrid
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.media.MediaSeriesGrid
import com.mxt.anitrend.ui.media.StudioGrid
import com.mxt.anitrend.ui.media.StudioRowUiState
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import com.mxt.anitrend.ui.staff.StaffGrid
import com.mxt.anitrend.ui.staff.StaffRowUiState

/** Sections rendered by the single Compose host in the favourites destination. */
internal enum class FavouriteGridSection {
    ANIME,
    MANGA,
    CHARACTERS,
    STAFF,
    STUDIOS,
}

/** Renders the selected favourite grid while the fragment owns tab state and paging. */
@Composable
internal fun FavouriteGridHost(
    section: FavouriteGridSection,
    mediaItems: List<MediaSeriesCardUiState>,
    characterItems: List<CharacterRowUiState>,
    staffItems: List<StaffRowUiState>,
    studioItems: List<StudioRowUiState>,
    gridState: LazyGridState,
    mediaColumnsResource: Int = R.integer.grid_giphy_x3,
    modifier: Modifier = Modifier,
    onMediaClick: (MediaSeriesCardUiState) -> Unit,
    onMediaLongClick: (MediaSeriesCardUiState) -> Unit,
    onCharacterClick: (Long) -> Unit,
    onStaffClick: (Long) -> Unit,
    onStudioClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    when (section) {
        FavouriteGridSection.ANIME, FavouriteGridSection.MANGA ->
            MediaSeriesGrid(
                items = mediaItems,
                modifier = modifier,
                listState = gridState,
                columnsResource = mediaColumnsResource,
                onMediaClick = onMediaClick,
                onMediaLongClick = onMediaLongClick,
                onLoadMore = onLoadMore,
            )
        FavouriteGridSection.CHARACTERS ->
            CharacterGrid(
                items = characterItems,
                modifier = modifier,
                gridState = gridState,
                onCharacterClick = onCharacterClick,
                onLoadMore = onLoadMore,
            )
        FavouriteGridSection.STAFF ->
            StaffGrid(
                items = staffItems,
                modifier = modifier,
                gridState = gridState,
                onStaffClick = onStaffClick,
                onLoadMore = onLoadMore,
            )
        FavouriteGridSection.STUDIOS ->
            StudioGrid(
                items = studioItems,
                modifier = modifier,
                gridState = gridState,
                onStudioClick = onStudioClick,
                onLoadMore = onLoadMore,
            )
    }
}
