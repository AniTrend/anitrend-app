package com.mxt.anitrend.base.plugin.image

import android.view.View
import com.mxt.anitrend.util.CompatUtil
import io.noties.markwon.*
import io.noties.markwon.core.CoreProps
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.image.ImageProps
import org.commonmark.node.Image

internal class ImageConfigurationPlugin private constructor(
) : AbstractMarkwonPlugin() {

    override fun configure(registry: MarkwonPlugin.Registry) {
        //val spacing = resources.getDimensionPixelSize(R.dimen.spacing_xl)
        //val maxWidth = resources.displayMetrics.widthPixels - spacing
        /*registry.require(ImagesPlugin::class.java) { plugin ->
            plugin.defaultMediaDecoder(
                DefaultDownScalingMediaDecoder.create(
                    maxWidth,
                    0
                )
            )
            plugin.addMediaDecoder(SvgPictureMediaDecoder.create())
        }*/
    }

    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.appendFactory(Image::class.java) { config: MarkwonConfiguration, props: RenderProps ->
            LinkSpan(
                config.theme(),
                ImageProps.DESTINATION.require(props),
                ImageLinkResolver(
                    CoreProps.LINK_DESTINATION.get(props),
                    config.linkResolver()
                )
            )
        }
    }

    private class ImageLinkResolver(
        val linkDestination: String?,
        val original: LinkResolver,
    ) : LinkResolver {
        override fun resolve(view: View, link: String) {
            if (linkDestination.isNullOrEmpty())
                CompatUtil.imagePreview(view, link)
            else
                original.resolve(view, linkDestination)
        }
    }

    companion object {
        fun create() = ImageConfigurationPlugin()
    }
}