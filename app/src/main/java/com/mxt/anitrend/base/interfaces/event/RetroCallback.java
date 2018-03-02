package com.mxt.anitrend.base.interfaces.event;

import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by max on 2017/10/14.
 * Annotated extension of retrofit callbacks
 */

public interface RetroCallback<T> extends Callback<T> {

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call the origination requesting object
     * @param response the response from the network
     */
    @Override
    void onResponse(@NonNull Call<T> call, @NonNull Response<T> response);

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call the origination requesting object
     * @param throwable contains information about the error
     */
    @Override
    void onFailure(@NonNull Call<T> call, @NonNull Throwable throwable);
}
