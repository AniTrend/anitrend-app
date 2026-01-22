package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/10/28.
 * Returns date formats such as started, starts, ended or ends
 */
class RangeDateTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SingleLineTextView(context, attrs, defStyleAttr) {

    companion object {
        @JvmStatic
        fun setStartDate(view: RangeDateTextView, fuzzyDate: FuzzyDate?) {
            view.text = String.format(
                "%s: %s",
                DateUtil.getStartTitle(fuzzyDate),
                DateUtil.convertDate(fuzzyDate)
            )
        }

        @JvmStatic
        fun setEndDate(view: RangeDateTextView, fuzzyDate: FuzzyDate?) {
            view.text = String.format(
                "%s: %s",
                DateUtil.getEndTitle(fuzzyDate),
                DateUtil.convertDate(fuzzyDate)
            )
        }
    }
}
