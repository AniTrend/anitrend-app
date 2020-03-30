package com.mxt.anitrend.extension

import android.content.Context
import androidx.annotation.StringRes
import org.koin.core.KoinComponent
import org.koin.core.get

object KoinExt : KoinComponent {

    /**
     * Helper to retrieve dependencies by class definition
     *
     * @param `class` registered class in koin modules
     */
    @JvmStatic
    fun <T : Any> get(`class`: Class<T>): T {
        return getKoin().get(`class`.kotlin, null, null)
    }

    fun getString(@StringRes text: Int): String? =
        runCatching {
            get<Context>().getString(text)
        }.also {
            it.exceptionOrNull()?.printStackTrace()
        }.getOrNull()

    fun getString(@StringRes text: Int, vararg values: String): String? =
        runCatching {
            get<Context>().getString(text, *values)
        }.also {
            it.exceptionOrNull()?.printStackTrace()
        }.getOrNull()
}