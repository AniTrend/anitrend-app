package com.mxt.anitrend.data.converter

import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import io.objectbox.converter.PropertyConverter

class ImageBaseConverter : PropertyConverter<ImageBase, String> {
    override fun convertToEntityProperty(databaseValue: String?): ImageBase? {
        return databaseValue?.let { WebFactory.gson.fromJson(it, ImageBase::class.java) }
    }

    override fun convertToDatabaseValue(entityProperty: ImageBase?): String? {
        return entityProperty?.let { WebFactory.gson.toJson(it) }
    }
}
