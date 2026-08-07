package com.mxt.anitrend.adapter.recycler.index

import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Focused tests for [ReviewHolderRegistry], the holder-to-review binding used by
 * [ReviewAdapter.onRateReviewResult] to route mutation outcomes to the visible vote
 * control. A holder rebound from review A to review B must drop the stale A mapping so
 * a late outcome for A can never reach (and mutate the vote control of) B.
 */
class ReviewHolderRegistryTest {

    @Test
    fun `holder rebound from review A to B drops stale A outcome and leaves B untouched`() {
        val registry = ReviewHolderRegistry<Any>()
        val holder = Any()
        val reviewA = 1L
        val reviewB = 2L

        registry.onBound(reviewA, holder)
        assertSame(holder, registry.holderFor(reviewA))

        // The same holder is rebound to review B without being recycled first.
        registry.onBound(reviewB, holder)

        // Delivering an outcome for A resolves no holder, so B's vote control is untouched.
        assertNull(registry.holderFor(reviewA))
        // B remains bound and routable.
        assertSame(holder, registry.holderFor(reviewB))
    }

    @Test
    fun `recycled holder is no longer routable`() {
        val registry = ReviewHolderRegistry<Any>()
        val holder = Any()

        registry.onBound(1L, holder)
        registry.onRecycled(holder)

        assertNull(registry.holderFor(1L))
    }

    @Test
    fun `rebinding a different holder for the same review replaces the mapping`() {
        val registry = ReviewHolderRegistry<Any>()
        val first = Any()
        val second = Any()

        registry.onBound(1L, first)
        registry.onBound(1L, second)

        assertSame(second, registry.holderFor(1L))
    }

    @Test
    fun `recycling the replaced holder does not remove the active mapping`() {
        val registry = ReviewHolderRegistry<Any>()
        val first = Any()
        val second = Any()

        registry.onBound(1L, first)
        registry.onBound(1L, second)
        registry.onRecycled(first)

        assertSame(second, registry.holderFor(1L))
    }
}
