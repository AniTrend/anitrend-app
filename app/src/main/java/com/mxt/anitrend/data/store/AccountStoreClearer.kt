package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.review.ReviewStore
import kotlinx.coroutines.runBlocking

class AccountStoreClearer(
    private val feedStore: FeedStore,
    private val mediaListStore: MediaListStore,
    private val reviewStore: ReviewStore,
    private val mutationRegistry: MutationRegistry,
) {
    fun clearAll() {
        runBlocking {
            feedStore.clear()
            mediaListStore.clear()
            reviewStore.clear()
            mutationRegistry.clearAll()
        }
    }
}
