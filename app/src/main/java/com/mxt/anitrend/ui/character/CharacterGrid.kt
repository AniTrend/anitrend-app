package com.mxt.anitrend.ui.character

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.tooling.preview.Preview
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatColorAttr
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect

/** Immutable character row projection; [id] drives navigation actions. */
internal data class CharacterRowUiState(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val isFavourite: Boolean,
) {
    companion object {
        fun from(model: CharacterBase): CharacterRowUiState = CharacterRowUiState(
            id = model.id,
            name = model.name?.fullName.orEmpty(),
            imageUrl = model.image?.let { it.extraLarge ?: it.large },
            isFavourite = model.isFavourite,
        )
    }
}

/**
 * Stateless character grid for the search and favourites character tabs. The
 * host fragment owns paging and state; this composable renders rows and emits
 * clicks. The legacy `GroupingUtil.wrapInGroup` step was an identity copy on
 * these tabs, so the grid renders flat rows.
 */
@Composable
internal fun CharacterGrid(
    items: List<CharacterRowUiState>,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    onCharacterClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    val context = LocalContext.current
    val cardColor = remember(context) { Color(context.getCompatColorAttr(R.attr.colorSurfaceContainerLow)) }
    val cardElevation = dimensionResource(R.dimen.sm_margin)

    LazyGridLoadMoreEffect(gridState, onLoadMore)
    LazyVerticalGrid(
        columns = GridCells.Fixed(integerResource(R.integer.grid_giphy_x3)),
        modifier = modifier.fillMaxSize(),
        state = gridState,
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> "character:${item.id}" },
            contentType = { _, _ -> "character" },
        ) { _, item ->
            Card(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cardElevation, vertical = cardElevation * 1.5f),
                shape = RoundedCornerShape(dimensionResource(R.dimen.lg_margin)),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            ) {
                CharacterRow(
                    characterName = item.name,
                    imageUrl = item.imageUrl,
                    isFavourite = item.isFavourite,
                    modifier = Modifier.padding(dimensionResource(R.dimen.xl_margin)),
                    onClick = { onCharacterClick(item.id) },
                )
            }
        }
    }
}

@Preview(name = "Character grid", showBackground = true)
@Composable
private fun CharacterGridPreview() {
    AniTrendTheme {
        CharacterGrid(
            items = listOf(
                CharacterRowUiState(id = 1L, name = "Character One", imageUrl = null, isFavourite = true),
                CharacterRowUiState(id = 2L, name = "Character Two", imageUrl = null, isFavourite = false),
            ),
            onCharacterClick = {},
            onLoadMore = {},
        )
    }
}
