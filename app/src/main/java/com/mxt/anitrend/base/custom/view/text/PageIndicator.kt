package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.extension.getCompatColor
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.util.CompatUtil

import java.util.Locale

/**
 * Created by max on 2017/11/25.
 */

class PageIndicator : AppCompatTextView, CustomView {

    var maximum: Int = 0

    constructor(context: Context) : super(context) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        val padding = CompatUtil.dipToPx(8f)

        setTextColor(context.getCompatColor(R.color.colorTextLight))
        background = context.getCompatDrawable(R.drawable.bubble_background)

        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setPadding(padding, padding, padding, padding)
        typeface = Typeface.DEFAULT_BOLD
    }

    fun setCurrentPosition(index: Int) {
        text = String.format(Locale.getDefault(), "%d / %d", index, maximum)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {

    }
}
