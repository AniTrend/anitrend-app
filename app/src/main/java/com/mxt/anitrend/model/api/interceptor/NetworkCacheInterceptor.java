package com.mxt.anitrend.model.api.interceptor;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mxt.anitrend.util.CompatUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by max on 2017/07/17.
 * Network cache injector interceptor
 */

public class NetworkCacheInterceptor implements Interceptor {

    private static final String CACHE_CONTROL = "Cache-Control";
    private Context mContext;
    private boolean forceCache;

    // re-write response header to force use of cache
    private CacheControl cacheControl = new CacheControl.Builder()
            .maxAge(6, TimeUnit.HOURS)
            .build();

    public NetworkCacheInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    public NetworkCacheInterceptor(Context mContext, boolean forceCache) {
        this.mContext = mContext;
        this.forceCache = forceCache;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if(!CompatUtil.isOnline(mContext) || !forceCache)
            return response.newBuilder().header(CACHE_CONTROL,
                    cacheControl.toString()).build();
        return response;
    }
}
