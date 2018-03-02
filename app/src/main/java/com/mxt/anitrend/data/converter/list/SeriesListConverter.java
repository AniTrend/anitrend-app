package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.Series;

import java.lang.reflect.Type;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/12/27.
 */

public class SeriesListConverter implements PropertyConverter<List<Series>, String> {

    @Override
    public List<Series> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        Type type = new TypeToken<List<Series>>() {}.getType();
        return WebFactory.gson.fromJson(databaseValue, type);
    }

    @Override
    public String convertToDatabaseValue(List<Series> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}