package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.text.util.LinkifyCompat
import androidx.appcompat.widget.AppCompatTextView
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.util.MarkDownUtil
import com.mxt.anitrend.util.RegexUtil
import org.commonmark.parser.Parser
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.MarkwonHtmlParserImpl
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.AsyncDrawableScheduler
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import java.util.Arrays.asList

class RichMarkdownTextView : AppCompatTextView, CustomView {

    constructor(context: Context) :
            super(context) { onInit() }

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs) { onInit() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { onInit() }

    val markwon by lazy {
        Markwon.builder(context)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(GlideImagesPlugin.create(context))
                .usePlugin(GlideImagesPlugin.create(Glide.with(context)))
                .usePlugin(GlideImagesPlugin.create(object : GlideImagesPlugin.GlideStore {
                    override fun cancel(target: Target<*>) {
                        Glide.with(context).clear(target)
                    }

                    override fun load(drawable: AsyncDrawable): RequestBuilder<Drawable> {
                        return Glide.with(context).load(drawable.destination)
                                .transition(DrawableTransitionOptions.withCrossFade(250))
                                .transform(
                                        CenterCrop(),
                                        RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.md_margin))
                                )
                    }
                })).build()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        isFocusable = false
        LinkifyCompat.addLinks(this, Linkify.WEB_URLS)
        movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {

    }

    fun setMarkDownText(markDownText: String?) {
        val strippedText = RegexUtil.removeTags(markDownText)
        val markdownSpan = MarkDownUtil.convert(strippedText, context, this)
        setText(markdownSpan, BufferType.SPANNABLE)
        //richMarkDown(this, markDownText)
    }
}
