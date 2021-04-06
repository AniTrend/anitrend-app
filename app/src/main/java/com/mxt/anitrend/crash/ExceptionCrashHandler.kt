/*
 * Copyright (C) 2020  AniTrend
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

package com.mxt.anitrend.crash

import android.annotation.SuppressLint
import android.util.Log
import com.mxt.anitrend.crash.contract.IExceptionCrashHandler
import timber.log.Timber
import kotlin.system.exitProcess

internal class ExceptionCrashHandler(
    private val handler: Thread.UncaughtExceptionHandler?
) : IExceptionCrashHandler {

    private fun handleUncaughtCrashException(thread: Thread, throwable: Throwable) {
        Timber.e(throwable, thread.name)
    }

    /**
     * @param thread Origin of crash
     * @param throwable Exception that was unhandled
     */
    @SuppressLint("LogNotTimber")
    override fun onException(thread: Thread?, throwable: Throwable?) {
        if (thread == null) return
        if (throwable == null) return
        try {
            handleUncaughtCrashException(thread, throwable)
        } catch (e: Exception) {
            // Timber may not have been initialized, so will log into the android logger
            Log.w("ExceptionCrashHandler", "Timber may not have been initialized yet, perhaps this crash happened before any DI configuration was complete", e)
            Log.e("ExceptionCrashHandler", "Original exception before timber threw -> ${thread.name} threw an exception which was unhandled", e)
        } finally {
            // terminate process after intercepting crash
            handler?.uncaughtException(thread, throwable)
            exitProcess(0)
        }
    }
}