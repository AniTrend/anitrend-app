package com.mxt.anitrend.binding

import androidx.annotation.StringRes
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView

fun RichMarkdownTextView.markDown(markdown: String?) {
    setMarkDownText(markdown)
}

fun RichMarkdownTextView.htmlText(html: String?) {
    setMarkDownText(html)
}

fun RichMarkdownTextView.basicText(html: String?) {
    setMarkDownText(html)
}

fun RichMarkdownTextView.htmlText(
    @StringRes resId: Int,
) {
    setMarkDownText(context.getString(resId))
}

fun RichMarkdownTextView.richMarkDown(markdown: String?) {
    // call after inflation and before setting markdown
    setMarkDownText(markdown)
}
