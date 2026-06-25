package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.base.interfaces.view.CustomView

/**
 * Created by max on 2017/06/24.
 * Single line text view widget
 */
open class SingleLineTextView
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

    override fun onViewRecycled() = Unit

    override fun onInit() {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.END
    }
}
