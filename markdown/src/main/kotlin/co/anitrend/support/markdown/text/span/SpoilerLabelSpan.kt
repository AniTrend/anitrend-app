package co.anitrend.support.markdown.text.span

import android.text.TextPaint
import android.text.style.CharacterStyle
import androidx.annotation.ColorInt

class SpoilerLabelSpan(
    @ColorInt private val backgroundColor: Int
) : CharacterStyle() {

    private var isHidden: Boolean = false

    override fun updateDrawState(textPaint: TextPaint?) {
        if (isHidden)
            textPaint?.color = backgroundColor
        textPaint?.bgColor = backgroundColor
    }
}