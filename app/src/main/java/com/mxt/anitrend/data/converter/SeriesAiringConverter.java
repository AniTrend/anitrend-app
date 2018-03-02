package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.general.Airing;
import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * Series Airing Property Converter
 */

public class SeriesAiringConverter implements PropertyConverter<Airing, String> {

    @Override
    public Airing convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, Airing.class);
    }

    @Override
    public String convertToDatabaseValue(Airing entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
