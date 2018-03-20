package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.container.attribute.GraphError;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by max on 2017/06/15.
 * ResponseError utility class
 */

public class ErrorUtil {

    private static final String TAG = "ErrorUtil";

    /**
     * Converts the response error response into an object.
     *
     * @return The error object, or null if an exception was encountered
     * @see Error
     */
    public static @NonNull String getError(@Nullable Response response) {
        try {
            if(response != null) {
                ResponseBody responseBody = response.errorBody();
                String message;
                if (responseBody != null && !(message = responseBody.string()).isEmpty()) {
                    Log.e(TAG, message);
                    Type tokenType = new TypeToken<GraphError>(){}.getType();
                    GraphError graphError = WebFactory.gson.fromJson(message, tokenType);
                    return graphError.toString();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Unexpected error encountered";
        }
        return "No error information found!";
    }
}
