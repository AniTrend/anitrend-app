package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.util.LinkifyCompat
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.util.markdown.RegexUtil
import io.noties.markwon.Markwon
import io.noties.markwon.utils.NoCopySpannableFactory
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.koin.core.KoinComponent
import org.koin.core.inject

class RichMarkdownTextView : AppCompatTextView, CustomView, KoinComponent {

    val markwon by inject<Markwon>()

    constructor(context: Context) :
            super(context) { onInit() }

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs) { onInit() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { onInit() }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        movementMethod = BetterLinkMovementMethod.newInstance()
        Linkify.addLinks(this, Linkify.WEB_URLS)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {

    }

    fun setMarkDownText(markDownText: String?) {
        val strippedText = RegexUtil.removeTags(markDownText)
        val markdownSpan = MarkDownUtil.convert(strippedText)
        setText(markdownSpan, BufferType.SPANNABLE)
        // TODO: Disabled markwon markdown rendering
        //richMarkDown(markDownText)
    }
}
