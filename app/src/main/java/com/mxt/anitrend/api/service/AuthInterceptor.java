package com.mxt.anitrend.api.service;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.api.core.Token;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by max on 2017/05/09.
 */

class AuthInterceptor implements Interceptor {

    private Token authToken;

    AuthInterceptor(Token token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header(BuildConfig.HEADER_KEY, authToken.getHeaderValuePresets());

        Request request = builder.build();
        return chain.proceed(request);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AuthInterceptor && ((AuthInterceptor) obj).authToken.getAccess_token().equals(this.authToken.getAccess_token());
    }
}
