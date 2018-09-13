package com.mxt.anitrend.base.custom.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.mxt.anitrend.R;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/06/10.
 * Glide custom module
 */
@GlideModule
public class GlideAppModule extends AppGlideModule {

    /**
     * If you’ve already migrated to the Glide v4 AppGlideModule and LibraryGlideModule, you can disable manifest parsing entirely.
     * Doing so can improve the initial startup time of Glide and avoid some potential problems with trying to parse metadata.
     * To disable manifest parsing, override the isManifestParsingEnabled() method in your AppGlideModule implementation:
     */
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        boolean isLowRamDevice = CompatUtil.isLowRamDevice(context);

        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(isLowRamDevice? 1 : 2).build();

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
        builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, storageCacheSize));

        // Setting default params for glide
        RequestOptions options = new RequestOptions()
                .format(isLowRamDevice ? DecodeFormat.PREFER_RGB_565 : DecodeFormat.PREFER_ARGB_8888)
                .timeout(KeyUtil.GLIDE_REQUEST_TIMEOUT)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(CompatUtil.getDrawable(context, R.drawable.ic_emoji_sweat));

        builder.setDefaultRequestOptions(options);
    }
}
