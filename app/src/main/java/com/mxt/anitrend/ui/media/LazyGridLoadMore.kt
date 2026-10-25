package com.mxt.anitrend.ui.media

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow

/**
 * Minimum visible-row threshold for prefetching manual pages.
 */
internal const val LOAD_MORE_VISIBLE_THRESHOLD = 9

/**
 * Emits [onLoadMore] when the grid scrolls within [LOAD_MORE_VISIBLE_THRESHOLD]
 * rows of its end. The host owns page bookkeeping and request in-flight guards.
 */
@Composable
internal fun LazyGridLoadMoreEffect(
    gridState: LazyGridState,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            LazyGridLoadSample(
                totalItemCount = info.totalItemsCount,
                visibleItemCount = info.visibleItemsInfo.size,
                firstVisibleItem = info.visibleItemsInfo.firstOrNull()?.index ?: 0,
            )
        }.collect { sample ->
            // Keep row-count changes observable even when both layouts are near the end.
            if (
                sample.totalItemCount > 0 &&
                (sample.totalItemCount - sample.visibleItemCount) <=
                (sample.firstVisibleItem + LOAD_MORE_VISIBLE_THRESHOLD)
            ) {
                onLoadMore()
            }
        }
    }
}

private data class LazyGridLoadSample(
    val totalItemCount: Int,
    val visibleItemCount: Int,
    val firstVisibleItem: Int,
)

/**
 * Holds automatic pagination until a successful request returns page rows.
 * Errors and empty pages leave the next automatic page blocked so a later
 * scroll cannot skip the failed or unproductive page.
 */
internal class ManualGridAppendGate {
    var canRequestNextPage: Boolean = true
        private set

    fun onRequestStarted() {
        canRequestNextPage = false
    }

    fun onRequestFailed() {
        canRequestNextPage = false
    }

    fun onRequestSucceeded(itemsInPage: Int) {
        canRequestNextPage = itemsInPage > 0
    }
}
