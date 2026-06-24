package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

/**
 * Created by max on 2017/12/24.
 * custom font single line text view
 */
class SingleLineFontTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SingleLineTextView(context, attrs, defStyleAttr) {
    override fun onInit() {
        super.onInit()
        val assetManager = context.assets
        typeface = Typeface.createFromAsset(assetManager, "fonts/Lobster-Regular.ttf")
    }

    companion object {
        @JvmStatic
        fun setCustomFontType(
            singleLineTextView: SingleLineTextView,
            fontName: String,
        ) {
            val fontPath = String.format("fonts/%s", fontName)
            val assetManager = singleLineTextView.context.assets
            singleLineTextView.typeface = Typeface.createFromAsset(assetManager, fontPath)
        }
    }
}
