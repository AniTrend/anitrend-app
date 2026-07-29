package com.mxt.anitrend.data.store.mutation

class RevisionProvider {
    private val revisions = mutableMapOf<ResourceKey, Long>()

    suspend fun nextRevision(resourceKey: ResourceKey): Long = synchronized(revisions) {
        val current = revisions[resourceKey] ?: 0L
        val next = current + 1
        revisions[resourceKey] = next
        next
    }
}
