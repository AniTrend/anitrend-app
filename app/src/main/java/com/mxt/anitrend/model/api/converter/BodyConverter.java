package com.mxt.anitrend.model.api.converter;

import android.support.annotation.NonNull;

import com.mxt.anitrend.model.api.retro.WebFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by max on 2017/10/22.
 * Body factory default implementation
 */

public final class BodyConverter<T> implements Converter<ResponseBody, T> {

    private final Type type;

    private BodyConverter(Type type) {
        this.type = type;
    }

    public static final class Factory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            return new BodyConverter<>(type);
        }
    }

    @Override
    public T convert(@NonNull ResponseBody responseBody) throws IOException {
        T result = null;
        try {
            if(!responseBody.string().isEmpty())
                result = WebFactory.gson.fromJson(responseBody.string(), type);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            responseBody.close();
        }
        return result;
    }
}
