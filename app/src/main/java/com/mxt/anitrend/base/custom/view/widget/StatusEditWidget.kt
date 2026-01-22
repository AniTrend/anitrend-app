package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.extension.getCompatDrawable

class StatusEditWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SingleLineTextView(context, attrs, defStyleAttr), CustomView {

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        val padding = resources.getDimensionPixelSize(R.dimen.spacing_small)
        setPadding(padding, padding, padding, padding)
        setCompoundDrawablesWithIntrinsicBounds(
            context.getCompatDrawable(R.drawable.ic_edit_green_600_18dp),
            null,
            null,
            null
        )
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() = Unit
}
