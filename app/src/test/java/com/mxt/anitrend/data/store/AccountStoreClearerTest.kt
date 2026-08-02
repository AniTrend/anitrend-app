package com.mxt.anitrend.data.store

import com.mxt.anitrend.data.store.favourite.FavouriteStore
import com.mxt.anitrend.data.store.feed.FeedStore
import com.mxt.anitrend.data.store.medialist.MediaListStore
import com.mxt.anitrend.data.store.mutation.MutationRegistry
import com.mxt.anitrend.data.store.mutation.SessionEpoch
import com.mxt.anitrend.data.store.review.ReviewStore
import com.mxt.anitrend.data.store.user.UserStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class AccountStoreClearerTest {

    @Test
    fun `clearAll clears every canonical store and mutation registry`() = runTest {
        val feedStore = mock(FeedStore::class.java)
        val mediaListStore = mock(MediaListStore::class.java)
        val reviewStore = mock(ReviewStore::class.java)
        val userStore = mock(UserStore::class.java)
        val favouriteStore = mock(FavouriteStore::class.java)
        val mutationRegistry = mock(MutationRegistry::class.java)
        val sessionEpoch = SessionEpoch()

        AccountStoreClearer(
            feedStore = feedStore,
            mediaListStore = mediaListStore,
            reviewStore = reviewStore,
            userStore = userStore,
            favouriteStore = favouriteStore,
            mutationRegistry = mutationRegistry,
            sessionEpoch = sessionEpoch,
        ).clearAll()

        assertEquals(1L, sessionEpoch.current())
        verify(feedStore).clear()
        verify(mediaListStore).clear()
        verify(reviewStore).clear()
        verify(userStore).clear()
        verify(favouriteStore).clear()
        verify(mutationRegistry).clearAll()
    }
}
