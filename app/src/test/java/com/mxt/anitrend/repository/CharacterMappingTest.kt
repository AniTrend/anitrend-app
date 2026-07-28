package com.mxt.anitrend.repository

import com.mxt.anitrend.graphql.generated.CharacterBaseData
import com.mxt.anitrend.graphql.generated.CharacterOverviewData
import com.mxt.anitrend.repository.mapper.toCharacterEntity
import com.mxt.anitrend.repository.mapper.toMediaCharacterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterMappingTest {

    @Test
    fun `maps character base to entity`() {
        val character = CharacterBaseData(
            character = CharacterBaseData.Character(
                id = 1,
                name = CharacterBaseData.CharacterName(
                    first = "Spike",
                    last = "Spiegel",
                    native = "スパイク・スピーゲル",
                    alternative = listOf("Spikey", null),
                ),
                image = CharacterBaseData.CharacterImage(
                    large = "large.jpg",
                    medium = "medium.jpg",
                ),
                isFavourite = true,
                siteUrl = "https://anilist.co/character/1",
            ),
        ).toCharacterEntity()

        assertEquals(1L, character.id)
        assertEquals("Spike", character.name?.first)
        assertEquals("Spiegel", character.name?.last)
        assertEquals("スパイク・スピーゲル", character.name?.original)
        assertEquals(listOf("Spikey"), character.name?.alternative)
        assertNull(character.image?.extraLarge)
        assertEquals("large.jpg", character.image?.large)
        assertEquals("medium.jpg", character.image?.medium)
        assertEquals("https://anilist.co/character/1", character.siteUrl)
        assertTrue(character.isFavourite)
    }

    @Test
    fun `maps character overview to media character entity`() {
        val character = CharacterOverviewData(
            character = CharacterOverviewData.Character(
                id = 2,
                description = "Space cowboy",
                name = CharacterOverviewData.CharacterName(
                    first = "Faye",
                    last = "Valentine",
                    native = "フェイ・ヴァレンタイン",
                    alternative = listOf("Poker Alice"),
                ),
                image = CharacterOverviewData.CharacterImage(
                    large = "large-faye.jpg",
                    medium = "medium-faye.jpg",
                ),
                isFavourite = false,
                siteUrl = "https://anilist.co/character/2",
            ),
        ).toMediaCharacterEntity()

        assertEquals(2L, character.id)
        assertEquals("Space cowboy", character.description)
        assertEquals("Faye", character.name?.first)
        assertEquals("Valentine", character.name?.last)
        assertEquals(listOf("Poker Alice"), character.name?.alternative)
        assertNull(character.image?.extraLarge)
        assertEquals("large-faye.jpg", character.image?.large)
        assertEquals("https://anilist.co/character/2", character.siteUrl)
        assertEquals(false, character.isFavourite)
    }

    @Test
    fun `throws on null character roots`() {
        val baseError = runCatching {
            CharacterBaseData(character = null).toCharacterEntity()
        }.exceptionOrNull()
        val overviewError = runCatching {
            CharacterOverviewData(character = null).toMediaCharacterEntity()
        }.exceptionOrNull()

        assertEquals(IllegalStateException::class.java, baseError!!::class.java)
        assertEquals(IllegalStateException::class.java, overviewError!!::class.java)
    }
}
