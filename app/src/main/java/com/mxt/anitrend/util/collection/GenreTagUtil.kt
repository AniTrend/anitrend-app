package com.mxt.anitrend.util.collection

import com.google.gson.reflect.TypeToken
import com.mxt.anitrend.base.interfaces.base.PreferenceConverter
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import java.lang.reflect.Type
import java.util.WeakHashMap

/**
 * Created by max on 2018/09/01.
 * Converter for genres and tags selection preference
 */
class GenreTagUtil : PreferenceConverter<Map<Int, String>> {
    companion object {
        @JvmStatic
        fun createTagSelectionMap(
            mediaTags: List<MediaTag>,
            selectedIndices: Array<Int>?,
        ): Map<Int, String>? {
            if (selectedIndices != null) {
                val tagMap: MutableMap<Int, String> = WeakHashMap()
                for (index in selectedIndices) {
                    tagMap[index] = mediaTags[index].name.orEmpty()
                }
                return tagMap
            }
            return null
        }

        @JvmStatic
        fun createGenreSelectionMap(
            genres: List<Genre>,
            selectedIndices: Array<Int>?,
        ): Map<Int, String>? {
            if (selectedIndices != null) {
                val genreMap: MutableMap<Int, String> = WeakHashMap()
                for (index in selectedIndices) {
                    genreMap[index] = genres[index].genre.orEmpty()
                }
                return genreMap
            }
            return null
        }

        @JvmStatic
        fun getMappedValues(selectedItems: Map<Int, String>?): List<String>? {
            if (!selectedItems.isNullOrEmpty()) {
                return selectedItems
                    .map { it.value }
                    .toList()
            }
            return null
        }
    }

    override fun convertToEntity(json: String?): Map<Int, String> {
        if (json == null) {
            return WeakHashMap()
        }
        val targetType: Type = object : TypeToken<Map<Int, String>>() {}.type
        return WebFactory.gson.fromJson(json, targetType)
    }

    override fun convertToJson(entity: Map<Int, String>?): String {
        if (entity == null) {
            WebFactory.gson.toJson(WeakHashMap<Int, String>())
        }
        return WebFactory.gson.toJson(entity)
    }
}
