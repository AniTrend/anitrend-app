package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.base.StudioBase;

import java.lang.reflect.Type;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/12/27.
 */

public class StudioSmallListConverter implements PropertyConverter<List<StudioBase>, String> {

    @Override
    public List<StudioBase> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        Type type = new TypeToken<List<StudioBase>>() {}.getType();
        return WebFactory.gson.fromJson(databaseValue, type);
    }

    @Override
    public String convertToDatabaseValue(List<StudioBase> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}