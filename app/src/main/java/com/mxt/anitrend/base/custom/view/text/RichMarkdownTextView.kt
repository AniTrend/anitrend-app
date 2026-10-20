package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.text.util.Linkify
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.base.interfaces.view.CustomView
import io.noties.markwon.Markwon
import me.saket.bettermovementmethod.BetterLinkMovementMethod

/**
 * View-only markdown TextView. The configured [Markwon] renderer is supplied by
 * the host through [setMarkDownText]; this view never resolves dependencies or
 * dispatches links itself.
 */
class RichMarkdownTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialTextView(context, attrs, defStyleAttr),
    CustomView {
    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        setTextIsSelectable(true)
        Linkify.addLinks(this, Linkify.WEB_URLS)
        movementMethod = BetterLinkMovementMethod.getInstance()
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        setOnLongClickListener(null)
        setOnClickListener(null)
    }

    fun setMarkDownText(markDownText: String, markwon: Markwon) {
        markwon.setMarkdown(this, markDownText)
    }
}
