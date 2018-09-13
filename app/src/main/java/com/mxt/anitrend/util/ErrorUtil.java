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

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by max on 2017/06/15.
 * ResponseError utility class
 */

public class ErrorUtil {

    private static final int HTTP_LIMIT_REACHED = 429;

    private static final String TAG = "ErrorUtil";
    private static final String Retry_After = "Retry-After";
    private static final String RateLimit_Limit = "X-RateLimit-Limit";
    private static final String RateLimit_Remaining = "X-RateLimit-Remaining";

    /**
     * Converts the response error response into an object.
     *
     * @return The error object, or null if an exception was encountered
     * @see Error
     */
    public static @NonNull String getError(@Nullable Response response) {
        try {
            if(response != null) {
                Headers headers = response.headers();
                ResponseBody responseBody = response.errorBody();
                String message, error;
                if (response.code() != HTTP_LIMIT_REACHED) {
                    if (responseBody != null && !TextUtils.isEmpty(message = responseBody.string()))
                        if (!TextUtils.isEmpty(error = getGraphQLError(message)))
                            return error;
                } else {
                    error = String.format("%s of %s requests remaining, please retry after %s seconds",
                            headers.get(RateLimit_Remaining), headers.get(RateLimit_Limit), headers.get(Retry_After));
                    return error;
                }
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
