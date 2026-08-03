package com.mxt.anitrend.data.store.mutation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ResourceKeyOperationKeyTest {

    @Test
    fun `user resource key is distinct per userId`() {
        assertNotEquals(ResourceKey.User(1L), ResourceKey.User(2L))
        assertEquals(ResourceKey.User(1L), ResourceKey.User(1L))
    }

    @Test
    fun `favourite resource keys are typed and distinct across types with the same id`() {
        val sameId = 42L
        val keys = setOf(
            ResourceKey.FavouriteAnime(sameId),
            ResourceKey.FavouriteManga(sameId),
            ResourceKey.FavouriteCharacter(sameId),
            ResourceKey.FavouriteStaff(sameId),
            ResourceKey.FavouriteStudio(sameId),
        )
        assertEquals(5, keys.size)
    }

    @Test
    fun `user and favourite resource keys never collide`() {
        val sameId = 7L
        assertNotEquals(ResourceKey.User(sameId), ResourceKey.FavouriteAnime(sameId))
        assertNotEquals(ResourceKey.User(sameId), ResourceKey.FavouriteStudio(sameId))
    }

    @Test
    fun `userFollow factory maps to user resource key and type`() {
        val operationKey = OperationKey.userFollow(12L)
        assertEquals(ResourceKey.User(12L), operationKey.resourceKey)
        assertEquals(OperationKey.Type.USER_FOLLOW, operationKey.type)
    }

    @Test
    fun `favourite factories map to their typed resource key and type`() {
        val id = 99L
        assertEquals(ResourceKey.FavouriteAnime(id), OperationKey.favouriteAnime(id).resourceKey)
        assertEquals(OperationKey.Type.FAVOURITE_ANIME, OperationKey.favouriteAnime(id).type)

        assertEquals(ResourceKey.FavouriteManga(id), OperationKey.favouriteManga(id).resourceKey)
        assertEquals(OperationKey.Type.FAVOURITE_MANGA, OperationKey.favouriteManga(id).type)

        assertEquals(ResourceKey.FavouriteCharacter(id), OperationKey.favouriteCharacter(id).resourceKey)
        assertEquals(OperationKey.Type.FAVOURITE_CHARACTER, OperationKey.favouriteCharacter(id).type)

        assertEquals(ResourceKey.FavouriteStaff(id), OperationKey.favouriteStaff(id).resourceKey)
        assertEquals(OperationKey.Type.FAVOURITE_STAFF, OperationKey.favouriteStaff(id).type)

        assertEquals(ResourceKey.FavouriteStudio(id), OperationKey.favouriteStudio(id).resourceKey)
        assertEquals(OperationKey.Type.FAVOURITE_STUDIO, OperationKey.favouriteStudio(id).type)
    }

    @Test
    fun `favourite operations with the same target id and different types are distinct`() {
        val id = 5L
        val operationKeys = setOf(
            OperationKey.favouriteAnime(id),
            OperationKey.favouriteManga(id),
            OperationKey.favouriteCharacter(id),
            OperationKey.favouriteStaff(id),
            OperationKey.favouriteStudio(id),
        )
        assertEquals(5, operationKeys.size)
    }
}
