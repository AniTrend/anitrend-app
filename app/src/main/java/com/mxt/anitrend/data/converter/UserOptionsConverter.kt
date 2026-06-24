package com.mxt.anitrend.data.converter

import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions
import io.objectbox.converter.PropertyConverter

/**
 * Created by max on 2018/03/22.
 */
class UserOptionsConverter : PropertyConverter<UserOptions, String> {
    override fun convertToEntityProperty(databaseValue: String?): UserOptions? = databaseValue?.let { WebFactory.gson.fromJson(it, UserOptions::class.java) }

    override fun convertToDatabaseValue(entityProperty: UserOptions?): String? = entityProperty?.let { WebFactory.gson.toJson(it) }
}
