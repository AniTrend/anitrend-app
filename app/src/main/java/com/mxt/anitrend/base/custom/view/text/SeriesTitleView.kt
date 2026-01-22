package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.base.MediaBase

/**
 * Created by max on 2017/11/13.
 * Custom text view to display appropriate
 * series tittle according to user preferences
 */
class SeriesTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SingleLineTextView(context, attrs, defStyleAttr), CustomView {

    override fun onInit() {
        super.onInit()
    }

    fun setTitle(mediaBase: MediaBase) {
        text = mediaBase.title?.userPreferred.orEmpty()
    }

    fun setTitle(mediaList: MediaList) {
        setTitle(mediaList.media)
    }

    fun setTitle(review: Review) {
        setTitle(review.media)
    }

    override fun onViewRecycled() = Unit
}
