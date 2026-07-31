package com.mxt.anitrend.data.store.mutation

import java.util.concurrent.atomic.AtomicLong

class RequestSequence {
    private val counter = AtomicLong(0L)

    fun next(): Long = counter.incrementAndGet()
}
