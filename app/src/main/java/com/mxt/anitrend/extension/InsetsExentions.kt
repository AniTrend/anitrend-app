/*
 * Copyright (C) 2021 AniTrend
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.extension

import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import com.mxt.anitrend.R

/**
 * Record the window's top inset so it can be applied when the bottom sheet is slide up
 * to meet the top edge of the screen.
 */
fun ViewGroup.applySystemBarsWindowInsetsListener() {
    setOnApplyWindowInsetsListener { view, windowInsets ->
        val windowInsetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets)
        val systemBars = WindowInsetsCompat.Type.systemBars()
        view.setTag(
            R.id.tag_system_window_inset_top,
            windowInsetsCompat.getInsets(systemBars),
        )
        windowInsets
    }
}
