package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.base.SeriesBase;

import java.lang.reflect.Type;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/12/27.
 */

public class SeriesSmallListConverter implements PropertyConverter<List<SeriesBase>, String> {

    @Override
    public List<SeriesBase> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        Type type = new TypeToken<List<SeriesBase>>() {}.getType();
        return WebFactory.gson.fromJson(databaseValue, type);
    }

    @Override
    public String convertToDatabaseValue(List<SeriesBase> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}