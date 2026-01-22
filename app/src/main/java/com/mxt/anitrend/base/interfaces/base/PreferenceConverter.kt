package com.mxt.anitrend.base.interfaces.base

/**
 * Created by max on 2018/09/01.
 * Convert objects to json values and back.
 */
interface PreferenceConverter<T> {
    fun convertToEntity(json: String?): T

    fun convertToJson(entity: T?): String
}
