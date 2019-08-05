package com.mxt.anitrend.data.converter

import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import io.objectbox.converter.PropertyConverter

class UserStatisticTypesConverter: PropertyConverter<UserStatisticTypes, String> {

    override fun convertToEntityProperty(databaseValue: String?): UserStatisticTypes? {
        return if (databaseValue == null) null else WebFactory.gson.fromJson(databaseValue, UserStatisticTypes::class.java)
    }

    override fun convertToDatabaseValue(entityProperty: UserStatisticTypes?): String? {
        return if (entityProperty == null) null else WebFactory.gson.toJson(entityProperty)
    }
}