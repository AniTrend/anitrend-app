package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.review.ReviewStore
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class AccountStoreClearerTest {

    @Test
    fun `clearAll clears every canonical store and mutation registry`() = runTest {
        val feedStore = mock(FeedStore::class.java)
        val mediaListStore = mock(MediaListStore::class.java)
        val reviewStore = mock(ReviewStore::class.java)
        val mutationRegistry = mock(MutationRegistry::class.java)

        AccountStoreClearer(
            feedStore = feedStore,
            mediaListStore = mediaListStore,
            reviewStore = reviewStore,
            mutationRegistry = mutationRegistry,
        ).clearAll()

        verify(feedStore).clear()
        verify(mediaListStore).clear()
        verify(reviewStore).clear()
        verify(mutationRegistry).clearAll()
    }
}
