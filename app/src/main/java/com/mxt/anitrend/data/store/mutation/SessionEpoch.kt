package com.mxt.anitrend.data.store.mutation

import java.util.concurrent.atomic.AtomicLong

class SessionEpoch {
    private val value = AtomicLong(0L)

    fun current(): Long = value.get()

    fun bump(): Long = value.incrementAndGet()
}
