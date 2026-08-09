package com.mxt.anitrend.adapter.recycler.shared

import androidx.paging.LoadState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadStateFooterUiStateTest {

    @Test
    fun `Loading state shows the spinner and hides the retry action`() {
        val state = LoadStateFooterUiState.from(LoadState.Loading)

        assertTrue(state.showLoading)
        assertFalse(state.showRetry)
    }

    @Test
    fun `Error state shows the retry action and hides the spinner`() {
        val state = LoadStateFooterUiState.from(LoadState.Error(RuntimeException("append failed")))

        assertFalse(state.showLoading)
        assertTrue(state.showRetry)
    }

    @Test
    fun `NotLoading with more pages hides the spinner and the retry action`() {
        val state = LoadStateFooterUiState.from(LoadState.NotLoading(endOfPaginationReached = false))

        assertFalse(state.showLoading)
        assertFalse(state.showRetry)
    }

    @Test
    fun `NotLoading with the end of pagination keeps the footer hidden`() {
        val state = LoadStateFooterUiState.from(LoadState.NotLoading(endOfPaginationReached = true))

        assertFalse(state.showLoading)
        assertFalse(state.showRetry)
    }
}
