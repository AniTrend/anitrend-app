package com.mxt.anitrend.base.custom.view.container

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.util.CompatUtil

/**
 * Created by max on 2017/11/30.
 * A base custom card view with pre applied styles
 *
 * app:contentPadding="@dimen/xl_margin"
 * app:cardUseCompatPadding="true"
 * app:cardPreventCornerOverlap="true"
 * app:cardCornerRadius="@dimen/xs_margin"
 * app:cardBackgroundColor="?attr/cardColor"
 */
open class CardViewBase
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialCardView(context, attrs, defStyleAttr),
    CustomView {
    init {
        onInit()
    }

    override fun onInit() {
        applyStyle(resources.getDimensionPixelSize(R.dimen.xl_margin))
    }

    protected fun applyStyle(contentPadding: Int) {
        radius = resources.getDimensionPixelSize(R.dimen.lg_margin).toFloat()
        useCompatPadding = true
        preventCornerOverlap = false
        setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding)
        setCardBackgroundColor(CompatUtil.getColorFromAttr(context, R.attr.cardColor))
        requestLayout()
    }

    override fun onViewRecycled() = Unit
}
