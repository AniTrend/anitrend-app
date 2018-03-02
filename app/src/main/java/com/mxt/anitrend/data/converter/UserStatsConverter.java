package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.general.UserStats;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * Entity Converter
 */

public class UserStatsConverter implements PropertyConverter<UserStats, String> {

    @Override
    public synchronized UserStats convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, UserStats.class);
    }

    @Override
    public synchronized String convertToDatabaseValue(UserStats entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
