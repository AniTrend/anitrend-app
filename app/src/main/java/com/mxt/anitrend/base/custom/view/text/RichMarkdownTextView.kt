package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.util.markdown.RegexUtil
import com.mxt.anitrend.view.sheet.BottomSheetMessage
import io.noties.markwon.Markwon
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class RichMarkdownTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr), CustomView, KoinComponent {

    private val markwon by inject<Markwon>()

    init { onInit() }

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

    }

    fun setMarkDownText(markDownText: String?) {
        //val strippedText = RegexUtil.removeTags(markDownText)
        //val markdownSpan = MarkDownUtil.convert(strippedText)
        //setText(markdownSpan, BufferType.SPANNABLE)
        markwon.setMarkdown(this, markDownText ?: "**No content available**")
    }
}
