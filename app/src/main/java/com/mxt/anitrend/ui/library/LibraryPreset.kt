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
package com.mxt.anitrend.ui.library

/**
 * The three Library presets rendered by one shared collection screen. The
 * preset shapes the rendered projection only; committed store entities and
 * [com.mxt.anitrend.data.store.medialist.MediaListQueryKey] stay preset-free.
 */
enum class LibraryPreset {
    /** `type=ANIME` with the selected status filter. */
    MY_ANIME,

    /** `type=MANGA` with the selected status filter and volume-aware progress. */
    MY_MANGA,

    /** `type=ANIME`, `statusIn=[CURRENT]`, projected to still-releasing entries. */
    AIRING,
}
