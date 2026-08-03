package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.domain.model.MediaListItemRenderModel
import com.mxt.anitrend.extension.getCompatColor
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.KeyUtil

/**
 * Created by max on 2017/10/27.
 * Custom status view of airing or publishing status
 */
class SeriesStatusWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView {
    init {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() = Unit

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() = Unit

    companion object {
        private fun applyStatus(
            view: SeriesStatusWidget,
            mediaStatus: String?,
        ) {
            val resolvedStatus = mediaStatus ?: KeyUtil.NOT_YET_RELEASED
            val colorRes =
                when (resolvedStatus) {
                    KeyUtil.RELEASING -> R.color.colorStateBlue
                    KeyUtil.FINISHED -> R.color.colorStateGreen
                    KeyUtil.NOT_YET_RELEASED -> R.color.colorStateOrange
                    else -> R.color.colorStateRed
                }
            view.setBackgroundColor(view.context.getCompatColor(colorRes))
        }

        /** Give the current airing status of the series */
        @JvmStatic
        fun setStatus(
            view: SeriesStatusWidget,
            model: MediaBase?,
        ) {
            applyStatus(view, model?.status)
        }

        /** Give the current airing status of the series */
        @JvmStatic
        fun setStatus(
            view: SeriesStatusWidget,
            model: Media?,
        ) {
            applyStatus(view, model?.status)
        }

        /** Give the current airing status of the series */
        @JvmStatic
        fun setStatus(
            view: SeriesStatusWidget,
            mediaList: MediaList?,
        ) {
            setStatus(view, mediaList?.media)
        }

        /** Give the current airing status of the series */
        @JvmStatic
        fun setStatus(
            view: SeriesStatusWidget,
            mediaStatus: String?,
        ) {
            applyStatus(view, mediaStatus)
        }

        /** Give the current airing status of the series */
        @JvmStatic
        fun setAiringStatus(
            view: SeriesStatusWidget,
            model: MediaListItemRenderModel?,
        ) {
            val nextAiring = model?.nextAiringEpisode
            if (model != null && nextAiring != null) {
                if (nextAiring.episode - model.progress > 1) {
                    view.setBackgroundColor(view.context.getCompatColor(R.color.colorStateYellow))
                    return
                }
            }
            setStatus(view, model?.mediaStatus)
        }
    }
}
