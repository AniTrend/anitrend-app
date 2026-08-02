package com.mxt.anitrend.data.store.user

import com.mxt.anitrend.domain.model.UserRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserStoreTest {

    @Test
    fun `upsert is deterministic`() = runTest {
        val store = InMemoryUserStore()
        val user = user(id = 1L, revision = 1L)

        store.apply(UserStoreChange.UserUpserted(user))
        val firstState = store.state.value

        store.apply(UserStoreChange.UserUpserted(user))
        val secondState = store.state.value

        assertEquals(firstState, secondState)
        assertEquals(user, store.state.value.usersById.getValue(1L))
    }

    @Test
    fun `observeUser emits committed record for the requested user only`() = runTest {
        val store = InMemoryUserStore()
        store.apply(UserStoreChange.UserUpserted(user(id = 1L, revision = 1L)))
        store.apply(UserStoreChange.UserUpserted(user(id = 2L, revision = 1L)))

        assertEquals(1L, store.observeUser(1L).first()?.id)
        assertEquals(2L, store.observeUser(2L).first()?.id)
        assertEquals(null, store.observeUser(3L).first())
    }

    @Test
    fun `newer revision wins and older revision is rejected`() = runTest {
        val store = InMemoryUserStore()
        store.apply(UserStoreChange.UserUpserted(user(id = 1L, name = "newer", isFollowing = true, revision = 5L)))
        store.apply(UserStoreChange.UserUpserted(user(id = 1L, name = "stale", isFollowing = false, revision = 4L)))

        val committed = store.state.value.usersById.getValue(1L)
        assertEquals("newer", committed.name)
        assertTrue(committed.isFollowing)
        assertEquals(5L, committed.revision)
    }

    @Test
    fun `equal revision accepts the upsert`() = runTest {
        val store = InMemoryUserStore()
        store.apply(UserStoreChange.UserUpserted(user(id = 1L, name = "first", revision = 2L)))
        store.apply(UserStoreChange.UserUpserted(user(id = 1L, name = "second", revision = 2L)))

        assertEquals("second", store.state.value.usersById.getValue(1L).name)
    }

    @Test
    fun `clear resets the store`() = runTest {
        val store = InMemoryUserStore()
        store.apply(UserStoreChange.UserUpserted(user(id = 1L, revision = 1L)))

        store.clear()

        assertTrue(store.state.value.usersById.isEmpty())
        assertFalse(store.state.value.usersById.containsKey(1L))
    }

    private fun user(
        id: Long,
        name: String = "user-$id",
        isFollowing: Boolean = false,
        revision: Long,
    ): UserRecord = UserRecord(
        id = id,
        name = name,
        avatar = "avatar-$id",
        banner = "banner-$id",
        isFollowing = isFollowing,
        revision = revision,
    )
}
