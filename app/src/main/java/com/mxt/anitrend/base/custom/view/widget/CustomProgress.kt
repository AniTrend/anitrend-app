package com.mxt.anitrend.base.custom.view.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.widget.ProgressBar
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.extension.getCompatColorAttr

/**
 * Created by max on 2017/07/01.
 * Custom progressbar
 */
class CustomProgress
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ProgressBar(context, attrs, defStyleAttr),
    CustomView {
    private var colorFilter: PorterDuffColorFilter? = null

    init {
        onInit()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        colorFilter =
            PorterDuffColorFilter(
                context.getCompatColorAttr(R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN,
            )
        applyColorFilter(progressDrawable)
        applyColorFilter(indeterminateDrawable)
    }

    private fun applyColorFilter(drawable: Drawable?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && drawable != null) {
            drawable.colorFilter = colorFilter
        }
    }

    override fun setProgressDrawable(drawable: Drawable) {
        applyColorFilter(drawable)
        super.setProgressDrawable(drawable)
    }

    override fun setIndeterminateDrawable(drawable: Drawable) {
        applyColorFilter(drawable)
        super.setIndeterminateDrawable(drawable)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() = Unit
}
