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
package com.mxt.anitrend.ui.discover

import com.mxt.anitrend.util.KeyUtil
import kotlinx.serialization.Serializable

/**
 * The editable Discover filter values plus the materialized defaults resolved
 * at ingress. Values are wire identifiers: empty lists stay empty here and are
 * normalized to null by the browse loader.
 */
@Serializable
internal data class DiscoverFilters(
    val sort: String? = null,
    val season: String? = null,
    val format: String? = null,
    val seasonYear: Int? = null,
    val startDateLike: String? = null,
    val status: String? = null,
    val genres: List<String>? = null,
    val tags: List<String>? = null,
    val isAdult: Boolean? = null,
)

/** A fully materialized Discover query for the shared browse loader. */
internal data class DiscoverQuery(
    val type: String?,
    val season: String?,
    val sort: String?,
    val isAdult: Boolean?,
    val format: String?,
    val seasonYear: Int?,
    val startDateLike: String?,
    val status: String?,
    val genres: List<String>?,
    val tags: List<String>?,
    val onList: Boolean?,
)

/** Saved per-`(mediaType, preset)` Discover state. */
@Serializable
internal data class DiscoverPresetState(
    val filters: DiscoverFilters = DiscoverFilters(),
    val page: Int = 1,
    val firstVisibleIndex: Int = 0,
    val firstVisibleOffset: Int = 0,
)

/**
 * Resolves the effective query for [preset]. Trending pins `TRENDING_DESC`,
 * Recently added pins `ID_DESC`, Browse uses the editable values, and For you
 * uses the favourite-derived [favouriteGenres]/[favouriteTags] with
 * `onList = false` and type ANIME. Manga never receives a season.
 */
internal fun discoverQuery(
    preset: DiscoverPreset,
    mediaType: String?,
    filters: DiscoverFilters,
    favouriteGenres: List<String>?,
    favouriteTags: List<String>?,
): DiscoverQuery {
    val isManga = mediaType == KeyUtil.MANGA
    val season = if (isManga) null else filters.season
    return when (preset) {
        DiscoverPreset.TRENDING ->
            DiscoverQuery(
                type = mediaType,
                season = season,
                sort = KeyUtil.TRENDING + KeyUtil.DESC,
                isAdult = filters.isAdult,
                format = filters.format,
                seasonYear = filters.seasonYear,
                startDateLike = filters.startDateLike,
                status = filters.status,
                genres = filters.genres,
                tags = filters.tags,
                onList = null,
            )

        DiscoverPreset.RECENT ->
            DiscoverQuery(
                type = mediaType,
                season = season,
                sort = KeyUtil.ID + KeyUtil.DESC,
                isAdult = filters.isAdult,
                format = filters.format,
                seasonYear = filters.seasonYear,
                startDateLike = filters.startDateLike,
                status = filters.status,
                genres = filters.genres,
                tags = filters.tags,
                onList = null,
            )

        DiscoverPreset.FOR_YOU ->
            DiscoverQuery(
                type = KeyUtil.ANIME,
                season = null,
                sort = filters.sort,
                isAdult = filters.isAdult,
                format = null,
                seasonYear = null,
                startDateLike = null,
                status = null,
                genres = favouriteGenres,
                tags = favouriteTags,
                onList = false,
            )

        DiscoverPreset.BROWSE ->
            DiscoverQuery(
                type = mediaType,
                season = season,
                sort = filters.sort,
                isAdult = filters.isAdult,
                format = filters.format,
                seasonYear = if (isManga) null else filters.seasonYear,
                startDateLike = if (isManga) filters.startDateLike else null,
                status = filters.status,
                genres = filters.genres,
                tags = filters.tags,
                onList = null,
            )
    }
}
