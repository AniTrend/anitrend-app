package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;

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

    private static final String ERROR_KEY = "error";
    private static final String ERROR_MESSAGE_KEY = "error_message";
    private static final String ERROR_DESCRIPTION_KEY = "error_description";

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
                    Type tokenType = new TypeToken<Map<String, String>>(){}.getType();
                    LinkedTreeMap<String, String> error = WebFactory.gson.fromJson(message, tokenType);
                    if (error != null && error.containsKey(ERROR_KEY) && error.containsKey(ERROR_MESSAGE_KEY))
                        return String.format(Locale.getDefault(), "%s: %s", error.get(ERROR_KEY), error.get(ERROR_MESSAGE_KEY));
                    else if (error != null && error.containsKey(ERROR_KEY) && error.containsKey(ERROR_DESCRIPTION_KEY))
                        return String.format(Locale.getDefault(), "%s: %s", error.get(ERROR_KEY), error.get(ERROR_DESCRIPTION_KEY));
                    else
                        return message;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
        return "No error information found!";
    }
}
