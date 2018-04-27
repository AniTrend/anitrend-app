package com.mxt.anitrend.model.api.interceptor;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mxt.anitrend.util.CompatUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by max on 2017/06/14.
 * cache injector interceptor
 */

public class CacheInterceptor implements Interceptor {

    private CacheControl cacheControl = new CacheControl
            .Builder().maxStale(3, TimeUnit.HOURS).build();

    private Context mContext;
    private boolean forceCache;

    public CacheInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    public CacheInterceptor(Context mContext, boolean forceCache) {
        this.mContext = mContext;
        this.forceCache = forceCache;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if(CompatUtil.isOnline(mContext) || forceCache) {
            Request original = chain.request().newBuilder()
                    .cacheControl(cacheControl).build();
            return chain.proceed(original);
        }

        return chain.proceed(chain.request());
    }
}
