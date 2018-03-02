package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.general.SeriesList;

import java.util.List;
import java.util.Map;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * User lists converter
 */

public class SeriesListTrackingConverter implements PropertyConverter<Map<String, List<SeriesList>>, String> {

    @Override
    public Map<String, List<SeriesList>> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, new TypeToken<Map<String, List<SeriesList>>>(){}.getType());
    }

    @Override
    public String convertToDatabaseValue(Map<String, List<SeriesList>> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
