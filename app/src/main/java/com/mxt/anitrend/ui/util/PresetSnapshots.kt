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
package com.mxt.anitrend.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Per-key screen state (filters, page and scroll) that survives configuration
 * changes. Keys identify the owner slot such as `(mediaType, preset)`; the map
 * is serialized only when the framework saves, so scroll recording never
 * triggers recomposition.
 */
internal class PresetSnapshots<T : Any>(
    val initial: T,
    private val serializer: KSerializer<T>,
) {
    private val states = linkedMapOf<String, T>()
    private val mapSerializer = MapSerializer(String.serializer(), serializer)
    private val json = Json { ignoreUnknownKeys = true }

    fun snapshotFor(key: String): T = states[key] ?: initial

    fun record(key: String, value: T) {
        states[key] = value
    }

    fun encodeForSave(): String = json.encodeToString(mapSerializer, states)

    fun restoreFrom(encoded: String) {
        states.putAll(json.decodeFromString(mapSerializer, encoded))
    }
}

/** Remembers a saveable [PresetSnapshots] holder for serializable [T] states. */
@Composable
internal fun <T : Any> rememberPresetSnapshots(
    initial: T,
    serializer: KSerializer<T>,
): PresetSnapshots<T> = rememberSaveable(
    saver =
    Saver(
        save = { holder -> holder.encodeForSave() },
        restore = { encoded -> PresetSnapshots(initial, serializer).apply { restoreFrom(encoded) } },
    ),
) {
    PresetSnapshots(initial, serializer)
}
