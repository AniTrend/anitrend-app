package com.mxt.anitrend.base.plugin.image

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin
import timber.log.Timber

internal class GlideImagePlugin private constructor(
    private val requestManager: RequestManager
) : GlideImagesPlugin.GlideStore {

    private val requestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            if (e != null)
                Timber.w(e, "Cannot load model -> $model")
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            if (resource is Animatable)
                resource.start()
            return false
        }
    }

    override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
        return requestManager.asDrawable()
            .addListener(requestListener)
            .load(drawable.destination)
    }

    override fun cancel(target: Target<*>) {
        requestManager.clear(target)
    }

    companion object {
        fun create(requestManager: RequestManager) =
            GlideImagePlugin(requestManager)
    }
}