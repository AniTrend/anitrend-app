package com.mxt.anitrend.adapter.recycler.shared

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

/**
 * Renders Paging load states onto the base-class screen surfaces: the base
 * progress/error/empty states for refresh, and the load-state footer for
 * append. Keeps the swipe-refresh indicators in sync with the refresh state.
 *
 * Effects are injected as callbacks so the decision logic stays framework-free
 * and unit-testable.
 */
class PagingLoadStateRenderer(
    private val itemCount: () -> Int,
    private val showLoading: () -> Unit,
    private val showContent: () -> Unit,
    private val showError: (String) -> Unit,
    private val showEmpty: (String) -> Unit,
    private val stopRefreshIndicators: () -> Unit,
    private val errorMessage: () -> String,
    private val emptyMessage: () -> String,
) {
    /** Projects the current combined state onto the injected screen surfaces. */
    fun render(loadStates: CombinedLoadStates) {
        val refresh = loadStates.refresh
        when {
            itemCount() > 0 -> {
                // Keep the swipe spinner while a refresh is in flight; otherwise stop it.
                if (refresh !is LoadState.Loading) {
                    stopRefreshIndicators()
                }
                showContent()
            }
            refresh is LoadState.Loading -> showLoading()
            refresh is LoadState.Error -> {
                stopRefreshIndicators()
                showError(refresh.error.message ?: errorMessage())
            }
            loadStates.append.endOfPaginationReached -> {
                stopRefreshIndicators()
                showEmpty(emptyMessage())
            }
            else -> showLoading()
        }
    }
}
