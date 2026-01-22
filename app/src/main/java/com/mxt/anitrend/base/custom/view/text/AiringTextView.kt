package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/10/27.
 * Shows information regarding airing
 */
class AiringTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SingleLineTextView(context, attrs, defStyleAttr) {

    override fun onInit() {
        super.onInit()
    }

    companion object {
        @JvmStatic
        fun setAiring(view: AiringTextView, mediaBase: MediaBase?) {
            if (mediaBase != null) {
                val nextAiringEpisode = mediaBase.nextAiringEpisode
                if (nextAiringEpisode != null)
                    view.text = DateUtil.getNextEpDate(nextAiringEpisode)
                else
                    view.text = CompatUtil.capitalizeWords(mediaBase.status)
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }
    }
}
