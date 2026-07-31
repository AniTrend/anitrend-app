package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.review.ReviewStore
import kotlinx.coroutines.runBlocking

class AccountStoreClearer(
    private val feedStore: FeedStore,
    private val mediaListStore: MediaListStore,
    private val reviewStore: ReviewStore,
    private val mutationRegistry: MutationRegistry,
    private val sessionEpoch: SessionEpoch,
) {
    fun clearAll() {
        // WebTokenRequest.invalidateInstance() is synchronous and is called from
        // legacy authentication/logout paths that immediately tear down session
        // state after this returns. Keep this blocking bridge for now so those
        // callers cannot observe partially cleared in-memory stores. The bounded
        // work here is limited to store mutex updates and registry resets.
        runBlocking {
            sessionEpoch.bump()
            feedStore.clear()
            mediaListStore.clear()
            reviewStore.clear()
            mutationRegistry.clearAll()
        }
    }
}
