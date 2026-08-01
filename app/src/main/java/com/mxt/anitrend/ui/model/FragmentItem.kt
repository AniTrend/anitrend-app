/*
 * Copyright (C) 2020 AniTrend
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
package com.mxt.anitrend.ui.model

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Fragment loader holder helper
 */
data class FragmentItem<T : Fragment>(
    val fragment: Class<out T>,
    val parameter: Bundle? = null,
    val tag: String = fragment.simpleName,
) {
    fun tag(): String = tag
}
