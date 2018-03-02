package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.base.CharacterBase;

import java.lang.reflect.Type;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/12/27.
 */

public class CharacterSmallListConverter implements PropertyConverter<List<CharacterBase>, String> {

    @Override
    public List<CharacterBase> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        Type type = new TypeToken<List<CharacterBase>>() {}.getType();
        return WebFactory.gson.fromJson(databaseValue, type);
    }

    @Override
    public String convertToDatabaseValue(List<CharacterBase> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}