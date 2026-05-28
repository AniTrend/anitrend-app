package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRatingBar
import com.mxt.anitrend.base.interfaces.view.CustomView

/**
 * Created by max on 2017/10/27.
 * Custom rating bar
 */
class CustomRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatRatingBar(context, attrs, defStyleAttr), CustomView {

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        setIsIndicator(true)
        stepSize = 0.1f
        numStars = 5
        max = 5
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() = Unit

    companion object {
        @JvmStatic
        fun setAverageScore(view: CustomRatingBar, meanScore: Int) {
            val rating = meanScore * view.max / 100f
            view.rating = rating
        }
    }
}
