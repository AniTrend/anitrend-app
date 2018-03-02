package com.mxt.anitrend.data.converter.list;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.base.StaffBase;

import java.lang.reflect.Type;
import java.util.List;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/12/27.
 */

public class StaffSmallListConverter implements PropertyConverter<List<StaffBase>, String> {

    @Override
    public List<StaffBase> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        Type type = new TypeToken<List<StaffBase>>() {}.getType();
        return WebFactory.gson.fromJson(databaseValue, type);
    }

    @Override
    public String convertToDatabaseValue(List<StaffBase> entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
