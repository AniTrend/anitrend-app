package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.R

class SeriesTypeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SingleLineTextView(context, attrs, defStyleAttr) {

    companion object {
        @JvmStatic
        fun setSeriesType(view: SeriesTypeView, seriesType: String?) {
            val attribute = view.context.getString(R.string.title_series_type)
            view.text = String.format("%s %s", attribute, seriesType)
        }
    }
}
