package com.mxt.anitrend.binding

import android.databinding.BindingAdapter
import android.support.annotation.StringRes
import android.text.Html
import android.widget.TextView
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.util.MarkDownUtil
import com.mxt.anitrend.util.RegexUtil

@BindingAdapter("markDown")
fun markDown(richMarkdownTextView: RichMarkdownTextView, markdown: String?) {
    val strippedText = RegexUtil.removeTags(markdown)
    val markdownSpan = MarkDownUtil.convert(strippedText, richMarkdownTextView.context, richMarkdownTextView)
    richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE)
}

@BindingAdapter("textHtml")
fun htmlText(richMarkdownTextView: RichMarkdownTextView, html: String) {
    val markdownSpan = MarkDownUtil.convert(html, richMarkdownTextView.context, richMarkdownTextView)
    richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE)
}

@BindingAdapter("basicHtml")
fun basicText(richMarkdownTextView: RichMarkdownTextView, html: String) {
    val htmlSpan = Html.fromHtml(html)
    richMarkdownTextView.text = htmlSpan
}

@BindingAdapter("textHtml")
fun htmlText(richMarkdownTextView: RichMarkdownTextView, @StringRes resId: Int) {
    val text = richMarkdownTextView.context.getString(resId)
    val markdownSpan = MarkDownUtil.convert(text, richMarkdownTextView.context, richMarkdownTextView)
    richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE)
}

@BindingAdapter("richMarkDown")
fun richMarkDown(richMarkdownTextView: RichMarkdownTextView, markdown: String?) {
    richMarkdownTextView.also {
        it.markwon.setMarkdown(it, RegexUtil.convertToStandardMarkdown(markdown))
    }
}