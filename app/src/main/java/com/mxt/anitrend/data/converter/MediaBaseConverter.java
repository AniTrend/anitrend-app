package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.base.MediaBase;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * Media small entity converter
 */

public class MediaBaseConverter implements PropertyConverter<MediaBase, String> {

    @Override
    public MediaBase convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, MediaBase.class);
    }

    @Override
    public String convertToDatabaseValue(MediaBase entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
