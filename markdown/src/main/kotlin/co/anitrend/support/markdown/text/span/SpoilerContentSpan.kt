package co.anitrend.support.markdown.text.span

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt

class SpoilerContentSpan(
    @ColorInt private val backgroundColor: Int,
    @ColorInt private val revealedTextColor: Int,
    private val spoilerContent: String
) : ClickableSpan() {

    private var isRevealed: Boolean = false

    /**
     * Performs the click action associated with this span.
     */
    override fun onClick(widget: View) {
        isRevealed = !isRevealed
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.isUnderlineText = false
        textPaint.bgColor = backgroundColor
        textPaint.color = if (isRevealed) revealedTextColor else backgroundColor
    }
}