package com.mxt.anitrend.ui.detail

import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.character.CharacterMediaHeaderRow
import com.mxt.anitrend.ui.character.CharacterMediaHeaderUiState
import com.mxt.anitrend.ui.character.CharacterRow
import com.mxt.anitrend.ui.character.CharacterRowUiState
import com.mxt.anitrend.ui.media.GroupTitleRow
import com.mxt.anitrend.ui.media.LazyGridLoadMoreEffect
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCard
import com.mxt.anitrend.ui.mediasearch.MediaSeriesCardUiState
import com.mxt.anitrend.ui.staff.StaffCharacterRoleRow
import com.mxt.anitrend.ui.staff.StaffRow
import com.mxt.anitrend.ui.staff.StaffRowUiState

internal sealed interface DetailGridRow {
    val key: String

    data class Header(
        override val key: String,
        val title: String,
        val count: Int,
    ) : DetailGridRow

    data class MediaSeries(
        override val key: String,
        val state: MediaSeriesCardUiState,
        val onClick: (View) -> Unit,
        val onLongClick: (View) -> Unit,
    ) : DetailGridRow

    data class Character(
        override val key: String,
        val state: CharacterRowUiState,
        val onClick: (View) -> Unit,
    ) : DetailGridRow

    data class Staff(
        override val key: String,
        val state: StaffRowUiState,
        val onClick: (View) -> Unit,
    ) : DetailGridRow

    data class CharacterMediaHeader(
        override val key: String,
        val state: CharacterMediaHeaderUiState,
        val onClick: (View) -> Unit,
        val onLongClick: (View) -> Unit,
    ) : DetailGridRow

    data class StaffCharacterRole(
        override val key: String,
        val state: StaffCharacterRoleUiState,
        val onClick: (View) -> Unit,
    ) : DetailGridRow
}

internal data class StaffCharacterRoleUiState(
    val characterId: Long,
    val characterName: String,
    val mediaId: Long,
    val mediaTitle: String,
    val coverImageUrl: String?,
    val isFavourite: Boolean,
)

internal data class DetailGridScrollPosition(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
)
internal data class DetailListAppendResult(
    val rows: List<DetailGridRow>,
    val addedContentCount: Int,
)

internal fun appendDetailRows(
    currentRows: List<DetailGridRow>,
    incomingRows: List<DetailGridRow>,
): DetailListAppendResult {
    if (incomingRows.isEmpty()) return DetailListAppendResult(currentRows, 0)

    val result = currentRows.toMutableList()
    val indicesByKey = HashMap<String, Int>(currentRows.size + incomingRows.size)
    fun rebuildIndices() {
        indicesByKey.clear()
        result.forEachIndexed { index, row -> indicesByKey[row.key] = index }
    }
    rebuildIndices()

    var addedContentCount = 0
    var changed = false
    var incomingIndex = 0
    while (incomingIndex < incomingRows.size) {
        val header = incomingRows[incomingIndex].takeIf(::isDetailGroupBoundary)
        val contentStart = if (header == null) incomingIndex else incomingIndex + 1
        var contentEnd = contentStart
        while (contentEnd < incomingRows.size && !isDetailGroupBoundary(incomingRows[contentEnd])) {
            contentEnd++
        }
        val contentRows = incomingRows.subList(contentStart, contentEnd)

        val existingHeaderIndex = header?.let { indicesByKey[it.key] }
        val headerIndex =
            when {
                header == null -> -1
                existingHeaderIndex == null -> {
                    result.add(header)
                    indicesByKey[header.key] = result.lastIndex
                    changed = true
                    if (header !is DetailGridRow.Header) addedContentCount++
                    result.lastIndex
                }
                header !is DetailGridRow.Header && result[existingHeaderIndex] != header -> {
                    result[existingHeaderIndex] = header
                    changed = true
                    existingHeaderIndex
                }
                else -> existingHeaderIndex
            }

        val insertIndex =
            if (headerIndex < 0) {
                result.size
            } else {
                var end = headerIndex + 1
                while (end < result.size && !isDetailGroupBoundary(result[end])) end++
                end
            }
        val additions = ArrayList<DetailGridRow>(contentRows.size)
        contentRows.forEach { row ->
            val existingIndex = indicesByKey[row.key]
            if (existingIndex == null) {
                additions.add(row)
            } else if (row !is DetailGridRow.Header && result[existingIndex] != row) {
                result[existingIndex] = row
                changed = true
            }
        }
        if (additions.isNotEmpty()) {
            result.addAll(insertIndex, additions)
            addedContentCount += additions.count { it !is DetailGridRow.Header }
            changed = true
            rebuildIndices()
        }

        if (
            header is DetailGridRow.Header &&
            existingHeaderIndex != null &&
            header.count > 0 &&
            additions.isNotEmpty()
        ) {
            val index = indicesByKey.getValue(header.key)
            val currentHeader = result[index] as DetailGridRow.Header
            result[index] = currentHeader.copy(count = currentHeader.count + additions.size)
            changed = true
        }

        incomingIndex = contentEnd
    }

    return DetailListAppendResult(
        rows = if (changed) result.toList() else currentRows,
        addedContentCount = addedContentCount,
    )
}

private fun isDetailGroupBoundary(row: DetailGridRow): Boolean = row is DetailGridRow.Header || row is DetailGridRow.CharacterMediaHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DetailListGrid(
    rows: List<DetailGridRow>,
    gridState: LazyGridState,
    callbackTarget: View,
    onLoadMore: () -> Unit,
    pendingScrollPosition: DetailGridScrollPosition? = null,
    canRestoreScrollPosition: Boolean = true,
    onScrollPositionRestored: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyGridLoadMoreEffect(gridState, onLoadMore)

    LaunchedEffect(pendingScrollPosition, rows.size, canRestoreScrollPosition) {
        val pending = pendingScrollPosition
        if (pending != null && rows.isNotEmpty() && canRestoreScrollPosition) {
            gridState.scrollToItem(
                index = pending.firstVisibleItemIndex.coerceIn(0, rows.lastIndex),
                scrollOffset = pending.firstVisibleItemScrollOffset,
            )
            onScrollPositionRestored()
        }
    }

    val columns = GridCells.Fixed(integerResource(R.integer.grid_giphy_x3))
    LazyVerticalGrid(
        columns = columns,
        state = gridState,
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = rows,
            key = DetailGridRow::key,
            span = { row ->
                if (row is DetailGridRow.Header || row is DetailGridRow.CharacterMediaHeader) {
                    GridItemSpan(maxLineSpan)
                } else {
                    GridItemSpan(1)
                }
            },
            contentType = { row ->
                when (row) {
                    is DetailGridRow.Header -> "group-header"
                    is DetailGridRow.MediaSeries -> "media-series"
                    is DetailGridRow.Character -> "character"
                    is DetailGridRow.Staff -> "staff"
                    is DetailGridRow.CharacterMediaHeader -> "character-media-header"
                    is DetailGridRow.StaffCharacterRole -> "staff-character-role"
                }
            },
        ) { row ->
            when (row) {
                is DetailGridRow.Header -> {
                    GroupTitleRow(
                        title = row.title,
                        count = row.count,
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.lg_margin)),
                    )
                }
                is DetailGridRow.MediaSeries -> {
                    MediaSeriesCard(
                        state = row.state,
                        onMediaClick = { row.onClick(callbackTarget) },
                        onMediaLongClick = { row.onLongClick(callbackTarget) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is DetailGridRow.Character -> {
                    CharacterRow(
                        characterName = row.state.name,
                        imageUrl = row.state.imageUrl,
                        isFavourite = row.state.isFavourite,
                        onClick = { row.onClick(callbackTarget) },
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.lg_margin)),
                    )
                }
                is DetailGridRow.Staff -> {
                    StaffRow(
                        state = row.state,
                        onClick = { row.onClick(callbackTarget) },
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.lg_margin)),
                    )
                }
                is DetailGridRow.CharacterMediaHeader -> {
                    CharacterMediaHeaderRow(
                        state = row.state,
                        onClick = { row.onClick(callbackTarget) },
                        onLongClick = { row.onLongClick(callbackTarget) },
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.lg_margin)),
                    )
                }
                is DetailGridRow.StaffCharacterRole -> {
                    StaffCharacterRoleRow(
                        characterName = row.state.characterName,
                        mediaTitle = row.state.mediaTitle,
                        imageUrl = row.state.coverImageUrl,
                        isFavourite = row.state.isFavourite,
                        onClick = { row.onClick(callbackTarget) },
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.lg_margin)),
                    )
                }
            }
        }
    }
}
