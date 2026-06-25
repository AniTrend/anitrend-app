package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import java.util.Locale

class SeriesYearTypeTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SingleLineTextView(context, attrs, defStyleAttr) {
    override fun onViewRecycled() {
        super.onViewRecycled()
    }

    override fun onInit() {
        super.onInit()
    }

    companion object {
        @JvmStatic
        fun htmlText(
            seriesYearTypeTextView: SeriesYearTypeTextView,
            mediaBase: MediaBase,
        ) {
            val context = seriesYearTypeTextView.context
            val startDate: FuzzyDate? = mediaBase.startDate
            val year =
                if (startDate?.isValidDate == true) {
                    String.format(Locale.getDefault(), "%d", startDate.year)
                } else {
                    context.getString(R.string.tba_placeholder)
                }
            when (mediaBase.type) {
                KeyUtil.ANIME ->
                    if (CompatUtil.equals(mediaBase.format, KeyUtil.MOVIE)) {
                        seriesYearTypeTextView.text =
                            String.format(
                                Locale.getDefault(),
                                "%s - %s",
                                year,
                                CompatUtil.capitalizeWords(mediaBase.format),
                            )
                    } else {
                        if (mediaBase.episodes > 0) {
                            seriesYearTypeTextView.text =
                                String.format(
                                    Locale.getDefault(),
                                    "%s - %s",
                                    year,
                                    context.getString(R.string.text_anime_episodes, mediaBase.episodes),
                                )
                        } else {
                            seriesYearTypeTextView.text =
                                String.format(
                                    Locale.getDefault(),
                                    "%s - %s",
                                    year,
                                    CompatUtil.capitalizeWords(mediaBase.format),
                                )
                        }
                    }
                KeyUtil.MANGA ->
                    if (mediaBase.chapters > 0) {
                        seriesYearTypeTextView.text =
                            String.format(
                                Locale.getDefault(),
                                "%s - %s",
                                year,
                                context.getString(R.string.text_manga_chapters, mediaBase.chapters),
                            )
                    } else {
                        seriesYearTypeTextView.text =
                            String.format(
                                Locale.getDefault(),
                                "%s - %s",
                                year,
                                CompatUtil.capitalizeWords(mediaBase.format),
                            )
                    }
            }
        }
    }
}
