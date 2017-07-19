package com.mxt.anitrend.api.interceptor;

import android.util.Log;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.api.core.Token;
import com.mxt.anitrend.async.TokenReference;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by max on 2017/05/09.
 */

public final class AuthInterceptor implements Interceptor {

    public AuthInterceptor() {
        //empty constructor
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        final Token temp = TokenReference.getInstance();
        if(temp != null) {
            Request.Builder builder = original.newBuilder()
                    .header(BuildConfig.HEADER_KEY, temp.getHeaderValuePresets());

            Request request = builder.build();
            return chain.proceed(request);
        }
        Log.e("AuthInterceptor", "Authentication reference is null, this should not happen under normal conditions");
        return chain.proceed(original);
    }
}
