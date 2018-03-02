package com.mxt.anitrend.model.api.interceptor;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by max on 2017/06/14.
 * Auth injector interceptor
 */

public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if(WebTokenRequest.getInstance() != null) {
            Request.Builder builder = chain.request().newBuilder()
                    .header(BuildConfig.HEADER_KEY, WebTokenRequest.getInstance().getHeader());
            Request request = builder.build();
            return chain.proceed(request);
        }
        Log.e("AuthInterceptor", "Authentication reference is null, this should not happen under normal conditions");
        return chain.proceed(chain.request());
    }
}
