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

package com.mxt.anitrend.crash.runtime

import com.mxt.anitrend.crash.ExceptionCrashHandler
import com.mxt.anitrend.crash.contract.IExceptionCrashHandler

/**
 * An uncaught exception handler for the application
 */
internal class UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {

    private val originalHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    private val exceptionHandler: IExceptionCrashHandler =
        ExceptionCrashHandler(originalHandler)

    /**
     * Method invoked when the given thread terminates due to the given uncaught exception.
     *
     * Any exception thrown by this method will be ignored by the Java Virtual Machine.
     *
     * @param thread the thread
     * @param throwable the exception
     */
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        exceptionHandler.onException(thread, throwable)
    }
}