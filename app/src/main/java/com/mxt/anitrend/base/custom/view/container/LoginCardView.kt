package com.mxt.anitrend.base.custom.view.container

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.R

class LoginCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardViewBase(context, attrs, defStyleAttr) {

    override fun onInit() {
        applyStyle(resources.getDimensionPixelSize(R.dimen.md_margin))
    }

    override fun onViewRecycled() = Unit
}
