package com.mxt.anitrend.data.converter

import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import io.objectbox.converter.PropertyConverter

/**
 * Created by max on 2018/03/22.
 */
class MediaListOptionsConverter : PropertyConverter<MediaListOptions, String> {
    override fun convertToEntityProperty(databaseValue: String?): MediaListOptions? {
        return databaseValue?.let { WebFactory.gson.fromJson(it, MediaListOptions::class.java) }
    }

    override fun convertToDatabaseValue(entityProperty: MediaListOptions?): String? {
        return entityProperty?.let { WebFactory.gson.toJson(it) }
    }
}
