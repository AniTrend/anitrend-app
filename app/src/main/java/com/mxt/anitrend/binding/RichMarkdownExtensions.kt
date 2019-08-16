package com.mxt.anitrend.binding

import androidx.databinding.BindingAdapter
import androidx.annotation.StringRes
import android.text.Html
import android.widget.TextView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.util.MarkDownUtil
import com.mxt.anitrend.util.RegexUtil

@BindingAdapter("markDown")
fun RichMarkdownTextView.markDown(markdown: String?) {
    val strippedText = RegexUtil.removeTags(markdown)
    val markdownSpan = MarkDownUtil.convert(strippedText, context, this)
    setText(markdownSpan, TextView.BufferType.SPANNABLE)
}

@BindingAdapter("textHtml")
fun RichMarkdownTextView.htmlText(html: String?) {
    val markdownSpan = MarkDownUtil.convert(html, context, this)
    setText(markdownSpan, TextView.BufferType.SPANNABLE)
}

@BindingAdapter("basicHtml")
fun RichMarkdownTextView.basicText(html: String?) {
    val htmlSpan = Html.fromHtml(html)
    text = htmlSpan
}

@BindingAdapter("textHtml")
fun RichMarkdownTextView.htmlText(@StringRes resId: Int) {
    val text = context.getString(resId)
    val markdownSpan = MarkDownUtil.convert(text, context, this)
    setText(markdownSpan, TextView.BufferType.SPANNABLE)
}

@BindingAdapter("richMarkDown")
fun RichMarkdownTextView.richMarkDown(markdown: String?) {
    val tagsStripped = RegexUtil.removeTags(markdown)
    val userTagsConverted = RegexUtil.findUserTags(tagsStripped)
    val standardMarkdown = RegexUtil.convertToStandardMarkdown(userTagsConverted)
    markwon.setMarkdown(this, standardMarkdown)

}