package com.mxt.anitrend.widget.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ProgressLayoutStateTests {

    @Test
    fun initialState_isContent() {
        assertSame(ProgressLayoutState.CONTENT, ProgressLayoutState.initial())
    }

    @Test
    fun transition_toLoading_returnsLoading() {
        assertSame(
            ProgressLayoutState.LOADING,
            ProgressLayoutState.transition(
                from = ProgressLayoutState.CONTENT,
                to = ProgressLayoutState.LOADING
            )
        )
    }

    @Test
    fun transition_toError_returnsError() {
        assertSame(
            ProgressLayoutState.ERROR,
            ProgressLayoutState.transition(
                from = ProgressLayoutState.LOADING,
                to = ProgressLayoutState.ERROR
            )
        )
    }

    @Test
    fun transition_toContent_returnsContent() {
        assertSame(
            ProgressLayoutState.CONTENT,
            ProgressLayoutState.transition(
                from = ProgressLayoutState.ERROR,
                to = ProgressLayoutState.CONTENT
            )
        )
    }

    @Test
    fun transition_sameState_returnsSameState() {
        assertSame(
            ProgressLayoutState.LOADING,
            ProgressLayoutState.transition(
                from = ProgressLayoutState.LOADING,
                to = ProgressLayoutState.LOADING
            )
        )
    }
}
