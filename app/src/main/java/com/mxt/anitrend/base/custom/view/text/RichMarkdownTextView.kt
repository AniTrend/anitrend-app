package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.util.LinkifyCompat
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.util.MarkDownUtil
import com.mxt.anitrend.util.RegexUtil
import io.noties.markwon.Markwon
import org.koin.core.KoinComponent
import org.koin.core.inject

class RichMarkdownTextView : AppCompatTextView, CustomView, KoinComponent {

    constructor(context: Context) :
            super(context) { onInit() }

    constructor(context: Context, attrs: AttributeSet) :
            super(context, attrs) { onInit() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { onInit() }

    val markwon by inject<Markwon>()

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
