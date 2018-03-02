package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.base.SeriesBase;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * Series small entity converter
 */

public class SeriesBaseConverter implements PropertyConverter<SeriesBase, String> {

    @Override
    public SeriesBase convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, SeriesBase.class);
    }

    @Override
    public String convertToDatabaseValue(SeriesBase entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
