package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2018/03/22.
 */

public class MediaListOptionsConverter implements PropertyConverter<MediaListOptions, String> {

    @Override
    public MediaListOptions convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, MediaListOptions.class);
    }

    @Override
    public String convertToDatabaseValue(MediaListOptions entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
