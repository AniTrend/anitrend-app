package com.mxt.anitrend.base.plugin.image

import android.content.res.Resources
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.glide.GlideImagesPlugin

internal class GlideImagePlugin private constructor(
    private val requestManager: RequestManager,
    private val resources: Resources
) : GlideImagesPlugin.GlideStore {

    private val requestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
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
                resource.start();
            return false
        }
    }

    override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {

        val request = requestManager.asDrawable()
            .addListener(requestListener)
            .load(drawable.destination)

        return request.transform(
            RoundedCorners(
                resources.getDimensionPixelSize(R.dimen.md_margin)
            )
        )
    }

    override fun cancel(target: Target<*>) {
        requestManager.clear(target)
    }

    companion object {
        fun create(requestManager: RequestManager, resources: Resources) =
            GlideImagePlugin(requestManager, resources)
    }
}