package com.mxt.anitrend.custom.glide;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.app.ActivityManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by max on 2017/07/14.
 * Glide module for setting up some options
 */

public class ConfigModule implements GlideModule {

    /**
     * Lazily apply options to a {@link GlideBuilder} immediately before the Glide singleton is
     * created.
     * <p>
     * <p>
     * This method will be called once and only once per implementation.
     * </p>
     *
     * @param context An Application {@link Context}.
     * @param builder The {@link GlideBuilder} that will be used to create Glide.
     */
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        // Increasing cache & pool by 25% - default is 250MB
        int memoryCacheSize = (int) (1.25 * calculator.getMemoryCacheSize());
        int bitmapPoolSize = (int) (1.25 * calculator.getBitmapPoolSize());
        int storageCacheSize = 1024 * 1024 * 350;
        if(context.getExternalCacheDir() != null) {
            long total = context.getExternalCacheDir().getTotalSpace();
            storageCacheSize = (int) (total*0.2);
        }

        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
        builder.setBitmapPool(new LruBitmapPool(bitmapPoolSize));
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, storageCacheSize));
        builder.setDecodeFormat(ActivityManagerCompat.isLowRamDevice(activityManager) ?
                DecodeFormat.PREFER_RGB_565 : DecodeFormat.PREFER_ARGB_8888);
    }

    /**
     * Lazily register components immediately after the Glide singleton is created but before any requests can be
     * started.
     * <p>
     * <p>
     * This method will be called once and only once per implementation.
     * </p>
     *
     * @param context An Application {@link Context}.
     * @param glide   The newly created Glide singleton.
     */
    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
