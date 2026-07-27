package com.mxt.anitrend.data.converter

import com.mxt.anitrend.model.api.retro.ServiceFactory
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import io.objectbox.converter.PropertyConverter

class UserStatisticTypesConverter : PropertyConverter<UserStatisticTypes, String> {
    override fun convertToEntityProperty(databaseValue: String?): UserStatisticTypes? = if (databaseValue == null) null else ServiceFactory.gson.fromJson(databaseValue, UserStatisticTypes::class.java)

    override fun convertToDatabaseValue(entityProperty: UserStatisticTypes?): String? = if (entityProperty == null) null else ServiceFactory.gson.toJson(entityProperty)
}
