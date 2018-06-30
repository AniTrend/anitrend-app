package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.container.attribute.GraphError;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;

import java.lang.reflect.Type;
import java.util.List;

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
                String message, error;
                if (responseBody != null && !TextUtils.isEmpty(message = responseBody.string()))
                    if(!TextUtils.isEmpty(error = getGraphQLError(message)))
                        return error;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Unexpected error encountered";
        }
        return "Unable to provide information regarding error!";
    }

    private static @Nullable String getGraphQLError(String errorJson) {
        Log.e(TAG, errorJson);
        Type tokenType = new TypeToken<GraphContainer<?>>(){}.getType();
        GraphContainer<?> graphContainer = WebFactory.gson.fromJson(errorJson, tokenType);
        List<GraphError> errors = graphContainer.getErrors();
        if (!CompatUtil.isEmpty(errors)) {
            StringBuilder builder = new StringBuilder();
            for (GraphError error : errors)
                builder.append(error.toString());
            return builder.toString();
        }
        return null;
    }
}
