package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.general.MediaList;

import java.util.List;
import java.util.Map;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * User lists converter
 */

public class SeriesListTrackingConverter implements PropertyConverter<Map<String, List<MediaList>>, String> {

    @Override
    public Map<String, List<MediaList>> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, new TypeToken<Map<String, List<MediaList>>>(){}.getType());
    }

    @Override
    public String convertToDatabaseValue(Map<String, List<MediaList>> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
