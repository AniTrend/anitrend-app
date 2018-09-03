package com.mxt.anitrend.base.interfaces.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by max on 2018/09/01.
 * Convert objects to json values and back.
 */
public interface PreferenceConverter<T> {

    @NonNull
    T convertToEntity(@Nullable String json);

    @NonNull
    String convertToJson(@Nullable T entity);
}
