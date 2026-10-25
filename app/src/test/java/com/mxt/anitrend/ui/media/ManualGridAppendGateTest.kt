package com.mxt.anitrend.ui.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualGridAppendGateTest {
    @Test
    fun failedAppendKeepsNextPageBlockedUntilRetryAddsRows() {
        val gate = ManualGridAppendGate()
        assertTrue(gate.canRequestNextPage)

        gate.onRequestStarted()
        gate.onRequestFailed()
        assertFalse(gate.canRequestNextPage)

        gate.onRequestStarted()
        gate.onRequestSucceeded(itemsInPage = 1)
        assertTrue(gate.canRequestNextPage)
    }

    @Test
    fun emptyAppendKeepsNextPageBlockedUntilRetryAddsRows() {
        val gate = ManualGridAppendGate()
        gate.onRequestStarted()
        gate.onRequestSucceeded(itemsInPage = 0)
        assertFalse(gate.canRequestNextPage)

        gate.onRequestStarted()
        gate.onRequestSucceeded(itemsInPage = 1)
        assertTrue(gate.canRequestNextPage)
    }
}
