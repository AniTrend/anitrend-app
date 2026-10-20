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

import kotlinx.serialization.Serializable

/**
 * Fully materialized browse query values pushed onto the navigation stack.
 *
 * Values are resolved exactly once at the external ingress boundary using
 * Settings and Bundle.containsKey tri-state resolution: explicit null
 * bypasses preference defaults, absent values receive them. Internal
 * callers construct normalized values directly, never Bundles.
 */
@Serializable
data class BrowseFilters(
    val mediaType: String? = null,
    val season: String? = null,
    val sort: String? = null,
    val isAdult: Boolean? = null,
    val format: String? = null,
    val seasonYear: Int? = null,
    val startDateLike: String? = null,
    val status: String? = null,
    val genres: List<String>? = null,
    val tags: List<String>? = null,
    val onList: Boolean? = null,
    val pageLimit: Int = 21,
    val filterEnabled: Boolean = true,
    val basicFilter: Boolean = false,
    val compactType: Boolean = false,
)
