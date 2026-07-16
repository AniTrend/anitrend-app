package com.mxt.anitrend.data.converter

import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.UserStats
import io.objectbox.converter.PropertyConverter

/**
 * Created by max on 2017/11/04.
 * Entity Converter
 */
@Suppress("DEPRECATION")
class UserStatsConverter : PropertyConverter<UserStats, String> {
    @Synchronized
    override fun convertToEntityProperty(databaseValue: String?): UserStats? = databaseValue?.let { WebFactory.gson.fromJson(it, UserStats::class.java) }

    @Synchronized
    override fun convertToDatabaseValue(entityProperty: UserStats?): String? = entityProperty?.let { WebFactory.gson.toJson(it) }
}
