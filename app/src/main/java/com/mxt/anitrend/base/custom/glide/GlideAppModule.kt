package com.mxt.anitrend.base.custom.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.mxt.anitrend.R
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/06/10.
 * Glide custom module
 */
@GlideModule
class GlideAppModule : AppGlideModule() {

    /**
     * If you’ve already migrated to the Glide v4 AppGlideModule and LibraryGlideModule, you can disable manifest parsing entirely.
     * Doing so can improve the initial startup time of Glide and avoid some potential problems with trying to parse metadata.
     * To disable manifest parsing, override the isManifestParsingEnabled() method in your AppGlideModule implementation:
     */
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val isLowRamDevice = CompatUtil.isLowRamDevice(context)

        val calculator = MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens((if (isLowRamDevice) 1 else 2).toFloat()).build()

        // Increasing cache & pool by 25% - default is 250MB
        val memoryCacheSize = (1.25 * calculator.memoryCacheSize).toInt()
        val bitmapPoolSize = (1.25 * calculator.bitmapPoolSize).toInt()
        var storageCacheSize = 1024 * 1024 * 350
        if (context.externalCacheDir != null) {
            val total = context.externalCacheDir!!.totalSpace
            storageCacheSize = (total * 0.2).toInt()
        }

        builder.setMemoryCache(LruResourceCache(memoryCacheSize.toLong()))
        builder.setBitmapPool(LruBitmapPool(bitmapPoolSize.toLong()))
        builder.setDiskCache(ExternalPreferredCacheDiskCacheFactory(context, storageCacheSize.toLong()))

        // Setting default params for glide
        val options = RequestOptions()
                .format(if (isLowRamDevice) DecodeFormat.PREFER_RGB_565 else DecodeFormat.PREFER_ARGB_8888)
                .timeout(KeyUtil.GLIDE_REQUEST_TIMEOUT)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .error(CompatUtil.getDrawable(context, R.drawable.ic_emoji_sweat))

        builder.setDefaultRequestOptions(options)
    }
}
