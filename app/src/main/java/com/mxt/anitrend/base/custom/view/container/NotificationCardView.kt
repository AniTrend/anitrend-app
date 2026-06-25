package com.mxt.anitrend.base.custom.view.container

import android.content.Context
import android.util.AttributeSet

class NotificationCardView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardViewBase(context, attrs, defStyleAttr) {
    override fun onInit() {
        applyStyle(0)
    }
}
