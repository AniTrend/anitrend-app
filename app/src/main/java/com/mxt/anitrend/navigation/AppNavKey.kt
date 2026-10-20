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

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation 3 route keys for every application destination.
 *
 * Keys carry only primitive identity: IDs and presentation-independent
 * arguments, never complete mutable entities. [MediaSectionKey] matches the
 * existing MediaSection semantics; serialization keeps the stacks
 * restorable across process recreation.
 */
@Serializable
sealed interface AppNavKey : NavKey {
    /** Discover root: browse, trending, recently added and for-you presets. */
    @Serializable
    data object DiscoverRoot : AppNavKey

    /** Library root: My Anime, My Manga and Airing presets. */
    @Serializable
    data object LibraryRoot : AppNavKey

    /** Social root: the activity feed. */
    @Serializable
    data object SocialRoot : AppNavKey

    /** Settings hub. */
    @Serializable
    data object Settings : AppNavKey

    /** One settings preference category. */
    @Serializable
    data class SettingsCategory(val categoryId: String) : AppNavKey

    /** Notifications destination. */
    @Serializable
    data object Notifications : AppNavKey

    /** About destination with support/update links. */
    @Serializable
    data object About : AppNavKey

    /** Structured release-notes timeline. */
    @Serializable
    data object Changelog : AppNavKey

    /** Application log viewer. */
    @Serializable
    data object Logging : AppNavKey

    /** Global search with entity chips. */
    @Serializable
    data class Search(val query: String? = null) : AppNavKey

    /** Review browsing with Anime/Manga chips. */
    @Serializable
    data class Reviews(val mediaType: String = "ANIME") : AppNavKey

    /** Direct messages with an optional deep-linked mailbox. */
    @Serializable
    data class Messages(val userId: Long? = null, val mailbox: String = "INBOX") : AppNavKey

    /** Activity thread with replies for one feed entry. */
    @Serializable
    data class Comment(val feedId: Long) : AppNavKey

    /** Media detail. */
    @Serializable
    data class Media(val mediaId: Long, val mediaType: String? = null) : AppNavKey

    /** Complete paged media section of one media entry. */
    @Serializable
    data class MediaCollection(val mediaId: Long, val mediaType: String?, val section: MediaSectionKey) : AppNavKey

    /** Character detail. */
    @Serializable
    data class Character(val characterId: Long) : AppNavKey

    /** Staff detail. */
    @Serializable
    data class Staff(val staffId: Long) : AppNavKey

    /** Studio detail. */
    @Serializable
    data class Studio(val studioId: Long) : AppNavKey

    /** User profile; userId zero requires a nonblank userName. */
    @Serializable
    data class Profile(val userId: Long, val userName: String? = null) : AppNavKey

    /** Another user's anime or manga list. */
    @Serializable
    data class UserMediaList(val userId: Long, val userName: String?, val mediaType: String?) : AppNavKey

    /** Favourites browser with entity chips. */
    @Serializable
    data class Favourites(val userId: Long, val userName: String? = null) : AppNavKey

    /** Shared content composer seeded by an incoming share. */
    @Serializable
    data class SharedContent(val text: String? = null, val subject: String? = null) : AppNavKey

    /** External episode feed with Latest/Popular chips. */
    @Serializable
    data class Episodes(val popular: Boolean = false) : AppNavKey

    /** Fully materialized browse results pushed from ingress or filters. */
    @Serializable
    data class DiscoverResults(val filters: BrowseFilters) : AppNavKey
}

/**
 * Paged media section identity matching the existing MediaSection
 * semantics. OVERVIEW is the default detail screen itself, not a pushed
 * collection.
 */
@Serializable
enum class MediaSectionKey {
    RELATIONS,
    RECOMMENDATIONS,
    STATS,
    CHARACTERS,
    STAFF,
    FEED,
    REVIEWS,
}

/**
 * Wraps a route with a unique visit identity so nonconsecutive visits to
 * the same entity keep distinct saveable state and entry ViewModels:
 * Media A -> Character B -> Media A.
 */
@Serializable
data class StackKey(
    val entryId: Long,
    val route: AppNavKey,
) : NavKey
