package com.mxt.anitrend.utils;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.mxt.anitrend.api.structure.Error;

import retrofit2.Response;

/**
 * Created by max on 2017/04/21.
 */

public class ErrorHandler {

    /**
     * Converts the response error body into an object.
     *
     * @return The error object, or null if an exception was encountered
     * @see Error
     */
    public static @NonNull Error getError(Response body) {
        Error error = new Error("Null Exception", "Response returned from the server is null!");
        if(body == null)
            return error;

        try {
            final String message = body.errorBody().string();
            if(!message.isEmpty())
                error = new Gson().fromJson(message, Error.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Error("Error: "+body.code(), body.raw().message().isEmpty()?"Origin of the error is unknown!":body.raw().message());
        }
        return error;
    }

}
