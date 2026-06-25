package com.mxt.anitrend.binding

import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.util.markdown.RegexUtil
import io.noties.markwon.utils.NoCopySpannableFactory

fun RichMarkdownTextView.markDown(markdown: String?) {
    if (!settings.experimentalMarkdown) {
        val strippedText = RegexUtil.removeTags(markdown)
        val markdownSpan = MarkDownUtil.convert(strippedText)
        setText(markdownSpan, TextView.BufferType.SPANNABLE)
    } else {
        richMarkDown(markdown)
    }
}

fun RichMarkdownTextView.htmlText(html: String?) {
    if (!settings.experimentalMarkdown) {
        val markdownSpan = MarkDownUtil.convert(html)
        setText(markdownSpan, TextView.BufferType.SPANNABLE)
    } else {
        richMarkDown(html)
    }
}

fun RichMarkdownTextView.basicText(html: String?) {
    if (!settings.experimentalMarkdown) {
        val htmlSpan = HtmlCompat.fromHtml(html.orEmpty(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        setText(htmlSpan, TextView.BufferType.SPANNABLE)
    } else {
        richMarkDown(html)
    }
}

fun RichMarkdownTextView.htmlText(
    @StringRes resId: Int,
) {
    if (!settings.experimentalMarkdown) {
        val text = context.getString(resId)
        val markdownSpan = MarkDownUtil.convert(text)
        setText(markdownSpan, TextView.BufferType.SPANNABLE)
    } else {
        richMarkDown(context.getString(resId))
    }
}

fun RichMarkdownTextView.richMarkDown(markdown: String?) {
    // call after inflation and before setting markdown
    if (!settings.experimentalMarkdown) {
        setSpannableFactory(NoCopySpannableFactory.getInstance())
        val tagsStripped = RegexUtil.removeTags(markdown)
        // val userTagsConverted = RegexUtil.findUserTags(tagsStripped)
        val standardMarkdown = RegexUtil.convertToStandardMarkdown(tagsStripped)
        markwon.setMarkdown(this, standardMarkdown)
    } else {
        setMarkDownText(markdown)
    }
}
