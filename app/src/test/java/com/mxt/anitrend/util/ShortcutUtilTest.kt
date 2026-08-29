package com.mxt.anitrend.util

import com.mxt.anitrend.view.activity.index.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Producer-to-route contract for dynamic shortcuts (NFR-004): every shortcut
 * type maps to the [MainActivity.EXTRA_ROUTE] value the host consumes, so a
 * cold launch lands on the shortcut's destination instead of the start
 * destination.
 */
class ShortcutUtilTest {

    @Test
    fun `established producers keep their routes`() {
        assertEquals(MainActivity.ROUTE_NOTIFICATIONS, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_NOTIFICATION))
        assertEquals(MainActivity.ROUTE_MEDIA_LIST, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_MY_ANIME))
        assertEquals(MainActivity.ROUTE_MEDIA_LIST, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_MY_MANGA))
        assertEquals(MainActivity.ROUTE_PROFILE, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_PROFILE))
        assertEquals(MainActivity.ROUTE_SEARCH, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_SEARCH))
    }

    @Test
    fun `airing feed and trending producers map to their destinations`() {
        assertEquals(MainActivity.ROUTE_AIRING, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_AIRING))
        assertEquals(MainActivity.ROUTE_FEED, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_FEEDS))
        assertEquals(MainActivity.ROUTE_TRENDING, ShortcutUtil.routeForShortcutType(KeyUtil.SHORTCUT_TRENDING))
    }

    @Test
    fun `unknown shortcut types carry no route`() {
        assertNull(ShortcutUtil.routeForShortcutType(Int.MAX_VALUE))
    }

    @Test
    fun `route wire values match the consumer constants`() {
        // The producer extras and the consumer branches share these literal
        // wire values; a mismatch would silently drop the route.
        assertEquals("airing", MainActivity.ROUTE_AIRING)
        assertEquals("feed", MainActivity.ROUTE_FEED)
        assertEquals("trending", MainActivity.ROUTE_TRENDING)
    }

    // ── NFR-004: deterministic budget-aware registration ──

    @Test
    fun `dynamic budget subtracts static shortcuts from the launcher max`() {
        // The platform shares the limit between static and dynamic shortcuts.
        assertEquals(7, ShortcutUtil.dynamicShortcutBudget(maxShortcutCount = 7, staticShortcutCount = 0))
        assertEquals(5, ShortcutUtil.dynamicShortcutBudget(maxShortcutCount = 7, staticShortcutCount = 2))
        assertEquals(4, ShortcutUtil.dynamicShortcutBudget(maxShortcutCount = 4, staticShortcutCount = 0))
        // An over-budget launcher yields zero, never a negative registration.
        assertEquals(0, ShortcutUtil.dynamicShortcutBudget(maxShortcutCount = 4, staticShortcutCount = 7))
    }

    @Test
    fun `selection keeps the documented priority when the budget cannot fit the full set`() {
        val builders = listOf(
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_TRENDING).build(),
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_MY_ANIME).build(),
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_NOTIFICATION).build(),
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_PROFILE).build(),
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_MY_MANGA).build(),
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_FEEDS).build(),
            ShortcutUtil.ShortcutBuilder().setShortcutType(KeyUtil.SHORTCUT_AIRING).build(),
        )
        // Input order is shuffled: the selected order must still be the
        // documented priority, independent of caller order.
        val selected = ShortcutUtil.selectShortcutBuilders(builders, 4).map { it.shortcutType }
        assertEquals(
            listOf(
                KeyUtil.SHORTCUT_NOTIFICATION,
                KeyUtil.SHORTCUT_MY_ANIME,
                KeyUtil.SHORTCUT_MY_MANGA,
                KeyUtil.SHORTCUT_PROFILE,
            ),
            selected,
        )
    }

    @Test
    fun `selection registers the new producers once the budget allows`() {
        val builders = ShortcutUtil.DYNAMIC_SHORTCUT_PRIORITY.map { type ->
            ShortcutUtil.ShortcutBuilder().setShortcutType(type).build()
        }
        val selected = ShortcutUtil.selectShortcutBuilders(builders, 7).map { it.shortcutType }
        assertEquals(ShortcutUtil.DYNAMIC_SHORTCUT_PRIORITY, selected)
        assertTrue(selected.contains(KeyUtil.SHORTCUT_AIRING))
        assertTrue(selected.contains(KeyUtil.SHORTCUT_FEEDS))
        assertTrue(selected.contains(KeyUtil.SHORTCUT_TRENDING))
    }

    @Test
    fun `empty budget selects nothing`() {
        val builders = ShortcutUtil.DYNAMIC_SHORTCUT_PRIORITY.map { type ->
            ShortcutUtil.ShortcutBuilder().setShortcutType(type).build()
        }
        assertTrue(ShortcutUtil.selectShortcutBuilders(builders, 0).isEmpty())
    }
}
