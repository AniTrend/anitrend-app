package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.meta.UserOptions;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2018/03/22.
 */

public class UserOptionsConverter implements PropertyConverter<UserOptions, String> {

    @Override
    public UserOptions convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, UserOptions.class);
    }

    @Override
    public String convertToDatabaseValue(UserOptions entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
