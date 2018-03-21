package com.mxt.anitrend.data.converter;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * Entity Converter
 */

public class StringListConverter implements PropertyConverter<List<String>, String> {

    @Override
    public synchronized List<String> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return new ArrayList<>();
        Type targetType = new TypeToken<ArrayList<String>>(){}.getType();
        return WebFactory.gson.fromJson(databaseValue, targetType);
    }

    @Override
    public synchronized String convertToDatabaseValue(List<String> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
