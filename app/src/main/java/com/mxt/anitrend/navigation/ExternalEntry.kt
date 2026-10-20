/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.navigation

import android.content.Intent
import android.text.TextUtils
import com.mxt.anitrend.ui.discover.DiscoverPreset
import com.mxt.anitrend.ui.library.LibraryPreset
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.markdown.RegexUtil

/** The three navigation roots owned by the drawer. */
internal enum class AppRoot {
    DISCOVER,
    LIBRARY,
    SOCIAL,
}

/**
 * A fully resolved external ingress: the pushed [AppNavKey] route chain,
 * the root/preset selection it implies, and the external finish semantics.
 *
 * `finishOnBack` is armed only for a cold direct-detail/share ingress on
 * the task root; an internal push pops normally and never re-arms it
 * (NFR-002/003).
 */
internal data class ExternalEntry(
    val keys: List<AppNavKey>,
    val root: AppRoot? = null,
    val libraryPreset: LibraryPreset? = null,
    val discoverPreset: DiscoverPreset? = null,
    val mediaType: String? = null,
    val finishOnBack: Boolean = false,
)

/**
 * Resolves an incoming launcher intent into the Navigation 3 key table.
 *
 * Resolution order mirrors the legacy dispatcher: share actions, the
 * `EXTRA_ROUTE` shortcut channel, then the manifest deep-link URI family.
 * Unknown or malformed ingress returns null so the caller keeps the
 * default root behavior; it never manufactures identity-zero details or
 * destroys the active stack. Historical primitive `ScreenParam` extras
 * are read only at this exported boundary.
 */
internal fun resolveExternalEntry(
    intent: Intent,
    isTaskRoot: Boolean,
    settings: Settings,
): ExternalEntry? {
    if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        return ExternalEntry(
            keys = listOf(AppNavKey.SharedContent(text = text, subject = subject)),
            finishOnBack = isTaskRoot,
        )
    }

    resolveRouteExtra(intent, settings)?.let { return it }

    resolveDeepLink(intent, isTaskRoot)?.let { return it }

    return null
}

/** Resolves the `EXTRA_ROUTE` shortcut channel into keys. */
private fun resolveRouteExtra(
    intent: Intent,
    settings: Settings,
): ExternalEntry? = when (intent.getStringExtra(EXTRA_ROUTE)) {
    ROUTE_SETTINGS -> ExternalEntry(keys = listOf(AppNavKey.Settings))
    ROUTE_NOTIFICATIONS -> ExternalEntry(keys = listOf(AppNavKey.Notifications))
    ROUTE_MESSAGES -> ExternalEntry(keys = listOf(AppNavKey.Messages()))
    ROUTE_LOGGING -> ExternalEntry(keys = listOf(AppNavKey.Logging))
    ROUTE_COMMENT -> positiveId(intent)?.let { ExternalEntry(keys = listOf(AppNavKey.Comment(it))) }
    ROUTE_STUDIO -> positiveId(intent)?.let { ExternalEntry(keys = listOf(AppNavKey.Studio(it))) }
    ROUTE_CHARACTER -> positiveId(intent)?.let { ExternalEntry(keys = listOf(AppNavKey.Character(it))) }
    ROUTE_STAFF -> positiveId(intent)?.let { ExternalEntry(keys = listOf(AppNavKey.Staff(it))) }
    ROUTE_PROFILE -> userRoute(intent, pushMediaList = false)
    ROUTE_MEDIA_LIST -> userRoute(intent, pushMediaList = true)
    ROUTE_FAVOURITES -> userFavouritesRoute(intent)
    ROUTE_SEARCH -> ExternalEntry(keys = listOf(AppNavKey.Search(query = intent.getStringExtra(KeyUtil.arg_search))))
    ROUTE_MEDIA -> positiveId(intent)?.let {
        ExternalEntry(keys = listOf(AppNavKey.Media(it, mediaType(intent))))
    }
    ROUTE_FEED -> ExternalEntry(keys = listOf(AppNavKey.SocialRoot), root = AppRoot.SOCIAL)
    ROUTE_AIRING -> ExternalEntry(
        keys = listOf(AppNavKey.LibraryRoot),
        root = AppRoot.LIBRARY,
        libraryPreset = LibraryPreset.AIRING,
    )
    ROUTE_TRENDING -> ExternalEntry(
        keys = listOf(AppNavKey.DiscoverRoot),
        root = AppRoot.DISCOVER,
        discoverPreset = DiscoverPreset.TRENDING,
    )
    ROUTE_MEDIA_BROWSE -> ExternalEntry(keys = listOf(AppNavKey.DiscoverResults(browseFilters(intent, settings))))
    else -> null
}

/**
 * Resolves the manifest URI deep-link family: `/anime`, `/manga`,
 * `/character`, `/staff`, `/actor`, `/studio`, `/activity` and `/user`
 * (with the `animelist`/`mangalist` suffix constructing Profile then a
 * pushed UserMediaList so Profile stays beneath the list, NFR-001).
 */
private fun resolveDeepLink(
    intent: Intent,
    isTaskRoot: Boolean,
): ExternalEntry? {
    val path = intent.data?.path ?: return null
    val matcher = RegexUtil.findIntentKeys(path) ?: return null
    val type = matcher.group(1) ?: return null
    val lastKey = matcher.group(matcher.groupCount()) ?: return null

    fun deepLinkId(): Long? = lastKey.split("/").firstOrNull()?.toLongOrNull()?.takeIf { it > 0L }

    val key: AppNavKey? =
        when (type) {
            KeyUtil.DEEP_LINK_ANIME -> deepLinkId()?.let { AppNavKey.Media(it, KeyUtil.ANIME) }
            KeyUtil.DEEP_LINK_MANGA -> deepLinkId()?.let { AppNavKey.Media(it, KeyUtil.MANGA) }
            KeyUtil.DEEP_LINK_CHARACTER -> deepLinkId()?.let { AppNavKey.Character(it) }
            // /actor and /staff are the same staff entity in AniList.
            KeyUtil.DEEP_LINK_ACTOR,
            KeyUtil.DEEP_LINK_STAFF,
            -> deepLinkId()?.let { AppNavKey.Staff(it) }
            KeyUtil.DEEP_LINK_STUDIO -> deepLinkId()?.let { AppNavKey.Studio(it) }
            KeyUtil.DEEP_LINK_ACTIVITY -> deepLinkId()?.let { AppNavKey.Comment(it) }
            KeyUtil.DEEP_LINK_USER -> return userDeepLink(lastKey, isTaskRoot)
            else -> null
        }
    return key?.let { ExternalEntry(keys = listOf(it), finishOnBack = isTaskRoot) }
}

/**
 * `/user/{name}` lands on the profile; `/user/{name}/animelist|/mangalist`
 * lands on the profile and then pushes a typed media list. A numeric first
 * segment is a user id, not a username, even with a list suffix (NFR-003).
 */
private fun userDeepLink(
    lastKey: String,
    isTaskRoot: Boolean,
): ExternalEntry {
    val segments = lastKey.split("/", limit = 2)
    val identity = segments.getOrNull(0)
    val listSuffix = segments.getOrNull(1)?.lowercase()
    val mediaType =
        when (listSuffix) {
            "animelist" -> KeyUtil.ANIME
            "mangalist" -> KeyUtil.MANGA
            else -> null
        }
    val profile =
        if (identity != null && TextUtils.isDigitsOnly(identity) && identity.isNotEmpty()) {
            AppNavKey.Profile(userId = identity.toLong())
        } else if (!identity.isNullOrBlank()) {
            AppNavKey.Profile(userId = 0L, userName = identity)
        } else {
            null
        } ?: return ExternalEntry(keys = emptyList(), finishOnBack = isTaskRoot)

    val keys =
        if (mediaType != null) {
            val list =
                AppNavKey.UserMediaList(
                    userId = profile.userId,
                    userName = profile.userName,
                    mediaType = mediaType,
                )
            listOf(profile, list)
        } else {
            listOf(profile)
        }
    return ExternalEntry(keys = keys, finishOnBack = isTaskRoot)
}

/** ROUTE_PROFILE and ROUTE_MEDIA_LIST share the user identity channel. */
private fun userRoute(
    intent: Intent,
    pushMediaList: Boolean,
): ExternalEntry? {
    val userId = intent.getLongExtra(KeyUtil.arg_id, 0L)
    val userName = intent.getStringExtra(KeyUtil.arg_userName)
    if (userId <= 0L && userName.isNullOrBlank()) return null
    val profile = AppNavKey.Profile(userId = userId, userName = userName)
    return if (pushMediaList) {
        ExternalEntry(
            keys =
            listOf(
                profile,
                AppNavKey.UserMediaList(
                    userId = userId,
                    userName = userName,
                    mediaType = intent.getStringExtra(KeyUtil.arg_mediaType),
                ),
            ),
        )
    } else {
        ExternalEntry(keys = listOf(profile))
    }
}

private fun userFavouritesRoute(intent: Intent): ExternalEntry? {
    val userId = intent.getLongExtra(KeyUtil.arg_id, 0L)
    val userName = intent.getStringExtra(KeyUtil.arg_userName)
    if (userId <= 0L && userName.isNullOrBlank()) return null
    return ExternalEntry(keys = listOf(AppNavKey.Favourites(userId = userId, userName = userName)))
}

private fun positiveId(intent: Intent): Long? = intent.getLongExtra(KeyUtil.arg_id, 0L).takeIf { it > 0L }

private fun mediaType(intent: Intent): String? = intent.getStringExtra(KeyUtil.arg_mediaType)

/**
 * Materializes effective browse values once at the ingress boundary:
 * present extras win, absent values receive preference defaults, and the
 * adult filter is masked by the flavor-allowed effective preference.
 */
private fun browseFilters(
    intent: Intent,
    settings: Settings,
): BrowseFilters {
    val extras = intent.extras
    val isAdult: Boolean? =
        if (extras?.containsKey(KeyUtil.arg_isAdult) == true) {
            extras.getBoolean(KeyUtil.arg_isAdult)
        } else if (!settings.displayAdultContent) {
            // Absent input behind a disabled adult preference filters adult
            // media out; an enabled preference leaves the filter unset.
            false
        } else {
            null
        }
    return BrowseFilters(
        mediaType = extras?.getString(KeyUtil.arg_mediaType),
        season =
        if (extras?.containsKey(KeyUtil.arg_season) == true) {
            extras.getString(KeyUtil.arg_season)
        } else {
            null
        },
        sort =
        if (extras?.containsKey(KeyUtil.arg_sort) == true) {
            extras.getString(KeyUtil.arg_sort)
        } else {
            null
        },
        isAdult = isAdult,
        format = extras?.getString(KeyUtil.arg_format),
        seasonYear =
        if (extras?.containsKey(KeyUtil.arg_seasonYear) == true) {
            extras.getInt(KeyUtil.arg_seasonYear) ?: 0
        } else {
            null
        },
        status = extras?.getString(KeyUtil.arg_status),
        genres = null,
        tags = null,
        onList = null,
        pageLimit = KeyUtil.PAGING_LIMIT,
    )
}

/**
 * Resolves the persisted `general_startup_page` preference into the
 * consolidated root/preset selection. Unknown values keep the current
 * Airing fallback; authenticated-only targets fall back to Airing too when
 * requested signed out, so the shell never starts on a failing
 * authenticated query.
 */
internal fun resolveStartupEntry(
    rawStartupPage: String?,
    isAuthenticated: Boolean,
): ExternalEntry = when (rawStartupPage) {
    "0" -> ExternalEntry(keys = listOf(AppNavKey.SocialRoot), root = AppRoot.SOCIAL)
    "1" -> ExternalEntry(
        keys = listOf(AppNavKey.DiscoverRoot),
        root = AppRoot.DISCOVER,
        discoverPreset = DiscoverPreset.BROWSE,
        mediaType = KeyUtil.ANIME,
    )
    "2" -> ExternalEntry(
        keys = listOf(AppNavKey.DiscoverRoot),
        root = AppRoot.DISCOVER,
        discoverPreset = DiscoverPreset.BROWSE,
        mediaType = KeyUtil.MANGA,
    )
    "3" -> ExternalEntry(
        keys = listOf(AppNavKey.DiscoverRoot),
        root = AppRoot.DISCOVER,
        discoverPreset = DiscoverPreset.TRENDING,
        mediaType = KeyUtil.ANIME,
    )
    "7" ->
        if (isAuthenticated) {
            ExternalEntry(
                keys = listOf(AppNavKey.DiscoverRoot),
                root = AppRoot.DISCOVER,
                discoverPreset = DiscoverPreset.FOR_YOU,
                mediaType = KeyUtil.ANIME,
            )
        } else {
            airingStartup()
        }
    "5" ->
        if (isAuthenticated) {
            ExternalEntry(keys = listOf(AppNavKey.LibraryRoot), root = AppRoot.LIBRARY, libraryPreset = LibraryPreset.MY_ANIME)
        } else {
            airingStartup()
        }
    "6" ->
        if (isAuthenticated) {
            ExternalEntry(keys = listOf(AppNavKey.LibraryRoot), root = AppRoot.LIBRARY, libraryPreset = LibraryPreset.MY_MANGA)
        } else {
            airingStartup()
        }
    "8" -> ExternalEntry(keys = listOf(AppNavKey.Reviews()))
    else -> airingStartup()
}

private fun airingStartup() = ExternalEntry(
    keys = listOf(AppNavKey.LibraryRoot),
    root = AppRoot.LIBRARY,
    libraryPreset = LibraryPreset.AIRING,
)

internal const val EXTRA_ROUTE = "extra_main_route"
internal const val ROUTE_SETTINGS = "settings"
internal const val ROUTE_NOTIFICATIONS = "notifications"
internal const val ROUTE_MESSAGES = "messages"
internal const val ROUTE_LOGGING = "logging"
internal const val ROUTE_COMMENT = "comment"
internal const val ROUTE_STUDIO = "studio"
internal const val ROUTE_CHARACTER = "character"
internal const val ROUTE_STAFF = "staff"
internal const val ROUTE_PROFILE = "profile"
internal const val ROUTE_MEDIA_LIST = "media_list"
internal const val ROUTE_FAVOURITES = "favourites"
internal const val ROUTE_SEARCH = "search"
internal const val ROUTE_MEDIA = "media"
internal const val ROUTE_FEED = "feed"
internal const val ROUTE_AIRING = "airing"
internal const val ROUTE_TRENDING = "trending"
internal const val ROUTE_MEDIA_BROWSE = "media_browse"
