package com.mxt.anitrend.data.converter;

import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule;

import io.objectbox.converter.PropertyConverter;

/**
 * Created by max on 2017/11/04.
 * Media AiringSchedule Property Converter
 */

public class SeriesAiringConverter implements PropertyConverter<AiringSchedule, String> {

    @Override
    public AiringSchedule convertToEntityProperty(String databaseValue) {
        if(databaseValue == null)
            return null;
        return WebFactory.gson.fromJson(databaseValue, AiringSchedule.class);
    }

    @Override
    public String convertToDatabaseValue(AiringSchedule entityProperty) {
        if(entityProperty == null)
            return null;
        return WebFactory.gson.toJson(entityProperty);
    }
}
