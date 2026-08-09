package com.mxt.anitrend.adapter.recycler.shared

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

/**
 * Renders Paging load states onto the base-class screen surfaces: the base
 * progress/error/empty states for refresh, and the load-state footer for
 * append. Keeps the swipe-refresh indicators in sync with the refresh state.
 *
 * Effects are injected through [Callbacks] so the decision logic stays
 * framework-free and unit-testable.
 */
class PagingLoadStateRenderer(
    private val itemCount: () -> Int,
    private val callbacks: Callbacks,
) {
    /** Projects the current combined state onto the injected screen surfaces. */
    fun render(loadStates: CombinedLoadStates) {
        val refresh = loadStates.refresh
        when {
            itemCount() > 0 -> {
                // Keep the swipe spinner while a refresh is in flight; otherwise stop it.
                if (refresh !is LoadState.Loading) {
                    callbacks.stopRefreshIndicators()
                }
                callbacks.showContent()
            }
            refresh is LoadState.Loading -> callbacks.showLoading()
            refresh is LoadState.Error -> {
                callbacks.stopRefreshIndicators()
                callbacks.showError(refresh.error.message ?: callbacks.errorMessage())
            }
            loadStates.append.endOfPaginationReached -> {
                callbacks.stopRefreshIndicators()
                callbacks.showEmpty(callbacks.emptyMessage())
            }
            else -> callbacks.showLoading()
        }
    }

    /**
     * Configuration holder for the fragment UI effects the renderer drives:
     * the base-class state surfaces, the swipe-refresh indicator settling, and
     * the fallback error and empty texts.
     */
    class Callbacks(
        val showLoading: () -> Unit,
        val showContent: () -> Unit,
        val showError: (String) -> Unit,
        val showEmpty: (String) -> Unit,
        val stopRefreshIndicators: () -> Unit,
        val errorMessage: () -> String,
        val emptyMessage: () -> String,
    )
}
