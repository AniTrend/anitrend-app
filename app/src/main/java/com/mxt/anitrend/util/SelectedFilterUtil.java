package com.mxt.anitrend.util;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.anilist.MediaTag;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2018/09/01.
 * Converter for genres and tags selection preference
 */

public class SelectedFilterUtil implements PropertyConverter<Map<Integer, String>, String> {

    @Override
    public @NonNull Map<Integer, String> convertToEntityProperty(@Nullable String databaseValue) {
        if(databaseValue == null)
            return new WeakHashMap<>();
        Type targetType = new TypeToken<Map<Integer, String>>(){}.getType();
        return WebFactory.gson.fromJson(databaseValue, targetType);
    }

    @Override
    public @NonNull String convertToDatabaseValue(@Nullable Map<Integer, String> entityProperty) {
        if(entityProperty == null)
            WebFactory.gson.toJson(new WeakHashMap<>());
        return WebFactory.gson.toJson(entityProperty);
    }

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
}
