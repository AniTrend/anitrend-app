package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.R
import com.mxt.anitrend.util.CompatUtil

class SpoilerTagTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SingleLineTextView(context, attrs, defStyleAttr) {

    companion object {
        @JvmStatic
        fun setIsSpoiler(view: SingleLineTextView, isSpoiler: Boolean?) {
            if (isSpoiler == true)
                view.setTextColor(CompatUtil.getColor(view.context, R.color.colorStateOrange))
        }
    }
}
