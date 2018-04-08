package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase;

import io.objectbox.converter.PropertyConverter;

public class ImageBaseConverter implements PropertyConverter<ImageBase, String> {

    @Override
    public ImageBase convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, ImageBase.class);
    }

    @Override
    public String convertToDatabaseValue(ImageBase entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
