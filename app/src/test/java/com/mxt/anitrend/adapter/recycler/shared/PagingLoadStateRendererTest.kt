package com.mxt.anitrend.adapter.recycler.shared

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import org.junit.Assert.assertEquals
import org.junit.Test

class PagingLoadStateRendererTest {

    @Test
    fun `content branch keeps the spinner while a refresh is in flight`() {
        val harness = Harness(itemCount = 1)

        harness.render(refresh = LoadState.Loading)

        assertEquals(listOf("showContent"), harness.calls)
    }

    @Test
    fun `content branch stops the indicators when refresh is idle`() {
        val harness = Harness(itemCount = 1)

        harness.render(refresh = LoadState.NotLoading(endOfPaginationReached = false))

        assertEquals(listOf("stopRefreshIndicators", "showContent"), harness.calls)
    }

    @Test
    fun `refresh loading with no items shows the loading state`() {
        val harness = Harness(itemCount = 0)

        harness.render(refresh = LoadState.Loading)

        assertEquals(listOf("showLoading"), harness.calls)
    }

    @Test
    fun `refresh error with no items shows the error message`() {
        val harness = Harness(itemCount = 0)

        harness.render(refresh = LoadState.Error(RuntimeException("boom")))

        assertEquals(listOf("stopRefreshIndicators", "showError(boom)"), harness.calls)
    }

    @Test
    fun `refresh error without a message falls back to the generic error text`() {
        val harness = Harness(itemCount = 0)

        harness.render(refresh = LoadState.Error(RuntimeException()))

        assertEquals(listOf("stopRefreshIndicators", "showError(generic error)"), harness.calls)
    }

    @Test
    fun `end of pagination with no items shows the empty state`() {
        val harness = Harness(itemCount = 0)

        harness.render(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            append = LoadState.NotLoading(endOfPaginationReached = true),
        )

        assertEquals(listOf("stopRefreshIndicators", "showEmpty(no results)"), harness.calls)
    }

    @Test
    fun `idle refresh with no items and more pages keeps loading`() {
        val harness = Harness(itemCount = 0)

        harness.render(refresh = LoadState.NotLoading(endOfPaginationReached = false))

        assertEquals(listOf("showLoading"), harness.calls)
    }

    private class Harness(itemCount: Int) {
        val calls = mutableListOf<String>()

        private val renderer =
            PagingLoadStateRenderer(
                itemCount = { itemCount },
                callbacks = PagingLoadStateRenderer.Callbacks(
                    showLoading = { calls += "showLoading" },
                    showContent = { calls += "showContent" },
                    showError = { message -> calls += "showError($message)" },
                    showEmpty = { message -> calls += "showEmpty($message)" },
                    stopRefreshIndicators = { calls += "stopRefreshIndicators" },
                    errorMessage = { "generic error" },
                    emptyMessage = { "no results" },
                ),
            )

        fun render(
            refresh: LoadState,
            append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        ) {
            renderer.render(states(refresh, append))
        }
    }
}

private fun states(
    refresh: LoadState,
    append: LoadState,
): CombinedLoadStates {
    val prepend = LoadState.NotLoading(endOfPaginationReached = true)
    return CombinedLoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append,
        source = LoadStates(refresh = refresh, prepend = prepend, append = append),
    )
}
