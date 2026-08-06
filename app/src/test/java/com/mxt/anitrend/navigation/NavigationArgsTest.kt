package com.mxt.anitrend.navigation

import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.navigation.extension.NavigationArgs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Base-behavior tests for the shared fragment navigation-bundle parsers. These
 * capture the exact containsKey / default / null-versus-empty semantics that the
 * migrated fragments must preserve (FeedListFragment, MediaBrowseFragment,
 * MediaLatestList, UserFeedFragment, MediaFeedFragment, MessageFeedFragment).
 */
class NavigationArgsTest {

    // ── intWithDefault (Bundle.getInt(key, default) semantics) ──

    @Test
    fun `intWithDefault returns stored value when key is present`() {
        assertEquals(25, NavigationArgs.intWithDefault(containsKey = true, value = 25, default = 50))
    }

    @Test
    fun `intWithDefault returns default when key is absent`() {
        assertEquals(50, NavigationArgs.intWithDefault(containsKey = false, value = 0, default = 50))
    }

    // ── booleanWithDefault (Bundle.getBoolean(key, default) semantics) ──

    @Test
    fun `booleanWithDefault returns stored value when key is present`() {
        assertEquals(false, NavigationArgs.booleanWithDefault(containsKey = true, value = false, default = true))
    }

    @Test
    fun `booleanWithDefault returns default when key is absent`() {
        assertEquals(true, NavigationArgs.booleanWithDefault(containsKey = false, value = false, default = true))
    }

    // ── optionalBoolean (containsKey tri-state) ──

    @Test
    fun `optionalBoolean returns null when key is absent`() {
        assertNull(NavigationArgs.optionalBoolean(containsKey = false, value = true))
    }

    @Test
    fun `optionalBoolean returns stored value when key is present`() {
        assertEquals(true, NavigationArgs.optionalBoolean(containsKey = true, value = true))
        assertEquals(false, NavigationArgs.optionalBoolean(containsKey = true, value = false))
    }

    // ── optionalString / optionalInt (containsKey tri-state) ──

    @Test
    fun `optionalString returns null when key is absent`() {
        assertNull(NavigationArgs.optionalString(containsKey = false, value = "x"))
    }

    @Test
    fun `optionalString returns stored value when key is present`() {
        assertEquals("x", NavigationArgs.optionalString(containsKey = true, value = "x"))
    }

    @Test
    fun `optionalInt returns null when key is absent`() {
        assertNull(NavigationArgs.optionalInt(containsKey = false, value = 7))
    }

    @Test
    fun `optionalInt returns stored value when key is present`() {
        assertEquals(7, NavigationArgs.optionalInt(containsKey = true, value = 7))
    }

    // ── optionalStringList (GraphQL null-versus-empty boundary) ──

    @Test
    fun `optionalStringList returns null when key is absent`() {
        // Semantically absent GraphQL list input must stay null, never an empty list.
        assertNull(NavigationArgs.optionalStringList(containsKey = false, value = listOf("Action")))
    }

    @Test
    fun `optionalStringList returns stored list when key is present`() {
        assertEquals(listOf("Action"), NavigationArgs.optionalStringList(containsKey = true, value = listOf("Action")))
    }

    // ── enum coercion ──

    @Test
    fun `resolveActivityType returns null for null or unknown raw value`() {
        assertNull(NavigationArgs.resolveActivityType(null))
        assertNull(NavigationArgs.resolveActivityType("NOT_A_TYPE"))
    }

    @Test
    fun `resolveActivityType coerces known raw value`() {
        assertEquals(ActivityType.MEDIA_LIST, NavigationArgs.resolveActivityType(ActivityType.MEDIA_LIST.name))
    }

    @Test
    fun `resolveMediaType returns null for null or unknown raw value`() {
        assertNull(NavigationArgs.resolveMediaType(null))
        assertNull(NavigationArgs.resolveMediaType("NOT_A_TYPE"))
    }

    @Test
    fun `resolveMediaType coerces known raw value`() {
        assertEquals(MediaType.ANIME, NavigationArgs.resolveMediaType(MediaType.ANIME.name))
        assertEquals(MediaType.MANGA, NavigationArgs.resolveMediaType(MediaType.MANGA.name))
    }
}
