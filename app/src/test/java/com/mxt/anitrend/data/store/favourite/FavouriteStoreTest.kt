package com.mxt.anitrend.data.store.favourite

import com.mxt.anitrend.domain.favourite.model.FavouriteKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavouriteStoreTest {

    @Test
    fun `flag replacement is deterministic`() = runTest {
        val store = InMemoryFavouriteStore()
        val key = FavouriteKey.Anime(1L)

        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(key, isFavourite = true, revision = 1L))
        val firstState = store.state.value

        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(key, isFavourite = true, revision = 1L))
        val secondState = store.state.value

        assertEquals(firstState, secondState)
        assertEquals(FavouriteFlag(key = key, isFavourite = true, revision = 1L), store.state.value.flagsByKey.getValue(key))
    }

    @Test
    fun `observeFavourite emits only the committed flag for the requested key`() = runTest {
        val store = InMemoryFavouriteStore()
        val anime = FavouriteKey.Anime(1L)
        val studio = FavouriteKey.Studio(1L)

        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(anime, isFavourite = true, revision = 1L))
        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(studio, isFavourite = false, revision = 1L))

        assertTrue(store.observeFavourite(anime).first()?.isFavourite == true)
        assertFalse(store.observeFavourite(studio).first()?.isFavourite == true)
        assertEquals(null, store.observeFavourite(FavouriteKey.Manga(1L)).first())
    }

    @Test
    fun `distinct keys with the same id do not overwrite each other`() = runTest {
        val store = InMemoryFavouriteStore()
        val anime = FavouriteKey.Anime(7L)
        val character = FavouriteKey.Character(7L)

        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(anime, isFavourite = true, revision = 1L))
        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(character, isFavourite = true, revision = 1L))

        assertEquals(2, store.state.value.flagsByKey.size)
    }

    @Test
    fun `newer revision wins and older revision is rejected`() = runTest {
        val store = InMemoryFavouriteStore()
        val key = FavouriteKey.Staff(3L)

        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(key, isFavourite = false, revision = 5L))
        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(key, isFavourite = true, revision = 4L))

        val committed = store.state.value.flagsByKey.getValue(key)
        assertFalse(committed.isFavourite)
        assertEquals(5L, committed.revision)
    }

    @Test
    fun `equal revision accepts the replacement`() = runTest {
        val store = InMemoryFavouriteStore()
        val key = FavouriteKey.Manga(9L)

        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(key, isFavourite = false, revision = 2L))
        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(key, isFavourite = true, revision = 2L))

        assertTrue(store.state.value.flagsByKey.getValue(key).isFavourite)
    }

    @Test
    fun `clear resets the store`() = runTest {
        val store = InMemoryFavouriteStore()
        store.apply(FavouriteStoreChange.FavouriteFlagReplaced(FavouriteKey.Anime(1L), isFavourite = true, revision = 1L))

        store.clear()

        assertTrue(store.state.value.flagsByKey.isEmpty())
    }
}
