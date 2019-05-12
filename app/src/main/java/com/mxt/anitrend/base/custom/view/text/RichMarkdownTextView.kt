package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.support.v4.text.util.LinkifyCompat
import android.support.v7.widget.AppCompatTextView
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import android.widget.TextView
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.util.MarkDownUtil
import com.mxt.anitrend.util.RegexUtil
import org.commonmark.parser.Parser
import ru.noties.markwon.AbstractMarkwonPlugin
import ru.noties.markwon.Markwon
import ru.noties.markwon.MarkwonConfiguration
import ru.noties.markwon.core.CorePlugin
import ru.noties.markwon.html.HtmlPlugin
import ru.noties.markwon.html.MarkwonHtmlParserImpl
import ru.noties.markwon.image.AsyncDrawableScheduler
import ru.noties.markwon.image.ImagesPlugin
import ru.noties.markwon.image.okhttp.OkHttpImagesPlugin
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
                .usePlugins(asList(
                        CorePlugin.create(),
                        ImagesPlugin.create(context),
                        OkHttpImagesPlugin.create(),
                        HtmlPlugin.create(),
                        object: AbstractMarkwonPlugin() {
                            @Override
                            override fun configureParser(builder: Parser.Builder) {

                            }

                            override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                                builder.htmlParser(MarkwonHtmlParserImpl.create())
                            }

                            override fun beforeSetText(textView: TextView, markdown: Spanned) {
                                AsyncDrawableScheduler.unschedule(textView)
                            }

                            override fun afterSetText(textView: TextView) {
                                AsyncDrawableScheduler.schedule(textView)
                            }
                        }
                ))
                .build()
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
