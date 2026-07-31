package com.mxt.anitrend.data.store.mutation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class KeyedMutex(
    private val coroutineScope: CoroutineScope,
) {
    private val mapMutex = Mutex()
    private val mutexes = mutableMapOf<ResourceKey, MutexEntry>()

    suspend fun <T> execute(
        resourceKey: ResourceKey,
        block: suspend () -> T,
    ): T {
        coroutineScope.coroutineContext.ensureActive()

        val entry = mapMutex.withLock {
            mutexes.getOrPut(resourceKey) { MutexEntry(mutex = Mutex()) }.also { mutexEntry ->
                mutexEntry.references += 1
            }
        }

        return try {
            entry.mutex.withLock {
                block()
            }
        } finally {
            release(resourceKey = resourceKey, entry = entry)
        }
    }

    private suspend fun release(
        resourceKey: ResourceKey,
        entry: MutexEntry,
    ) {
        withContext(NonCancellable) {
            mapMutex.withLock {
                val currentEntry = mutexes[resourceKey]
                if (currentEntry === entry) {
                    currentEntry.references -= 1
                    if (currentEntry.references == 0 && !currentEntry.mutex.isLocked) {
                        mutexes.remove(resourceKey)
                    }
                }
            }
        }
    }

    internal suspend fun trackedKeyCount(): Int = mapMutex.withLock {
        mutexes.size
    }

    internal suspend fun trackedKeys(): Set<ResourceKey> = mapMutex.withLock {
        mutexes.keys.toSet()
    }

    private data class MutexEntry(
        val mutex: Mutex,
        var references: Int = 0,
    )
}
