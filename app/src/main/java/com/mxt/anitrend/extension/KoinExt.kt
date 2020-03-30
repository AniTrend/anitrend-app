package com.mxt.anitrend.extension

import org.koin.core.KoinComponent

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
}