package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.UpdateUser
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UserTitleLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Focused tests for the `UpdateUser` settings slice: request variable mapping and
 * generated transport to [com.mxt.anitrend.domain.user.model.UserSettingsRecord]
 * mapping.
 */
class UserSettingsRecordMapperTest {

    // ── Request mapping ─────────────────────────────────────────────────────

    @Test
    fun `request maps provided settings values onto variables`() {
        val request = UpdateUser.request(
            about = "Hello world",
            airingNotifications = true,
            displayAdultContent = false,
            profileColor = "blue",
            rowOrder = "CUSTOM",
            scoreFormat = ScoreFormat.POINT_100,
            titleLanguage = UserTitleLanguage.ROMAJI,
        )

        val variables = request.variables
        assertNotNull(variables)
        assertEquals("Hello world", variables?.about)
        assertEquals(true, variables?.airingNotifications)
        assertEquals(false, variables?.displayAdultContent)
        assertEquals("blue", variables?.profileColor)
        assertEquals("CUSTOM", variables?.rowOrder)
        assertEquals(ScoreFormat.POINT_100, variables?.scoreFormat)
        assertEquals(UserTitleLanguage.ROMAJI, variables?.titleLanguage)
    }

    @Test
    fun `request keeps all variables null when nothing is provided`() {
        val variables = UpdateUser.request().variables
        assertNotNull(variables)

        assertNull(variables?.about)
        assertNull(variables?.airingNotifications)
        assertNull(variables?.displayAdultContent)
        assertNull(variables?.profileColor)
        assertNull(variables?.rowOrder)
        assertNull(variables?.scoreFormat)
        assertNull(variables?.titleLanguage)
    }

    @Test
    fun `request keeps untouched fields null on a partial update`() {
        val variables = UpdateUser.request(titleLanguage = UserTitleLanguage.NATIVE).variables
        assertNotNull(variables)

        assertEquals(UserTitleLanguage.NATIVE, variables?.titleLanguage)
        assertNull(variables?.about)
        assertNull(variables?.airingNotifications)
        assertNull(variables?.displayAdultContent)
        assertNull(variables?.profileColor)
        assertNull(variables?.rowOrder)
        assertNull(variables?.scoreFormat)
    }

    // ── Response mapping ────────────────────────────────────────────────────

    @Test
    fun `maps generated transport preserving values and enum wire names`() {
        val data = updateUserData(
            about = "Bio",
            options = UpdateUserData.UpdateUserOptions(
                airingNotifications = true,
                displayAdultContent = false,
                profileColor = "pink",
                titleLanguage = UserTitleLanguage.ENGLISH_STYLISED,
            ),
            mediaListOptions = UpdateUserData.UpdateUserMediaListOptions(
                rowOrder = "CUSTOM",
                scoreFormat = ScoreFormat.POINT_10_DECIMAL,
            ),
        )

        val record = data.toUserSettingsRecord()

        assertEquals(42L, record.id)
        assertEquals("Bio", record.about)
        assertEquals("ENGLISH_STYLISED", record.titleLanguage)
        assertEquals(false, record.displayAdultContent)
        assertEquals(true, record.airingNotifications)
        assertEquals("pink", record.profileColor)
        assertEquals("POINT_10_DECIMAL", record.scoreFormat)
        assertEquals("CUSTOM", record.rowOrder)
    }

    @Test
    fun `maps absent nested blocks to null settings values`() {
        val record = updateUserData(options = null, mediaListOptions = null).toUserSettingsRecord()

        assertEquals(42L, record.id)
        assertNull(record.about)
        assertNull(record.titleLanguage)
        assertNull(record.displayAdultContent)
        assertNull(record.airingNotifications)
        assertNull(record.profileColor)
        assertNull(record.scoreFormat)
        assertNull(record.rowOrder)
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    private fun updateUserData(
        about: String? = null,
        options: UpdateUserData.UpdateUserOptions?,
        mediaListOptions: UpdateUserData.UpdateUserMediaListOptions?,
    ): UpdateUserData.UpdateUser = UpdateUserData.UpdateUser(
        about = about,
        avatar = null,
        bannerImage = null,
        id = 42,
        isFollowing = null,
        mediaListOptions = mediaListOptions,
        name = "mxt",
        options = options,
        updatedAt = null,
    )
}
