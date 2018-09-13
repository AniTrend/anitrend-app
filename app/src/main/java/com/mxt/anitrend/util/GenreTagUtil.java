package com.mxt.anitrend.util;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Stream;
import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.base.interfaces.base.PreferenceConverter;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by max on 2018/09/01.
 * Converter for genres and tags selection preference
 */

public class GenreTagUtil implements PreferenceConverter<Map<Integer, String>> {

    public static @Nullable Map<Integer, String> createTagSelectionMap(@NonNull List<MediaTag> mediaTags, @Nullable Integer[] selectedIndices) {
        if (selectedIndices != null) {
            Map<Integer, String> tagMap = new WeakHashMap<>();
            for (Integer index: selectedIndices)
                tagMap.put(index, mediaTags.get(index).getName());
            return tagMap;
        }
        return null;
    }

    public static @Nullable Map<Integer, String> createGenreSelectionMap(@NonNull List<Genre> genres, @Nullable Integer[] selectedIndices) {
        if (selectedIndices != null) {
            Map<Integer, String> genreMap = new WeakHashMap<>();
            for (Integer index: selectedIndices)
                genreMap.put(index, genres.get(index).getGenre());
            return genreMap;
        }
        return null;
    }

    public static @Nullable List<String> getMappedValues(@Nullable Map<Integer,String> selectedItems) {
        if (selectedItems != null && selectedItems.size() > 0)
            return Stream.of(selectedItems).map(Map.Entry::getValue)
                    .toList();
        return null;
    }

    @Override
    public @NonNull Map<Integer, String> convertToEntity(@Nullable String json) {
        if(json == null)
            return new WeakHashMap<>();
        Type targetType = new TypeToken<Map<Integer, String>>(){}.getType();
        return WebFactory.gson.fromJson(json, targetType);
    }

    @Override
    public @NonNull String convertToJson(@Nullable Map<Integer, String> entity) {
        if(entity == null)
            WebFactory.gson.toJson(new WeakHashMap<>());
        return WebFactory.gson.toJson(entity);
    }
}
