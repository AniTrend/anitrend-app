package com.mxt.anitrend.base.custom.view.image

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable

/**
 * Created by max on 2017/12/03.
 */
class AppCompatTintImageView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr),
    CustomView {
    override fun onInit() = Unit

    override fun onViewRecycled() = Unit

    fun setTintDrawable(
        @DrawableRes drawable: Int,
        @ColorRes colorTint: Int,
    ) {
        setImageDrawable(context.getCompatDrawable(drawable, colorTint))
    }

    fun setTintDrawableAttr(
        @DrawableRes drawable: Int,
        @AttrRes colorAttribute: Int,
    ) {
        setImageDrawable(context.getCompatTintedDrawable(drawable))
    }

    companion object {
        @JvmStatic
        fun setTintDrawable(
            imageView: AppCompatTintImageView,
            @DrawableRes drawable: Int,
        ) {
            imageView.setImageDrawable(
                imageView.context.getCompatTintedDrawable(drawable),
            )
        }
    }
}
