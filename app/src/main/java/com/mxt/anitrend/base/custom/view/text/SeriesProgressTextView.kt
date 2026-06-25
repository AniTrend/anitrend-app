package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.util.AttributeSet
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.media.MediaUtil
import java.util.Locale

/**
 * Created by max on 2017/10/29.
 * Episode Text View with
 */
class SeriesProgressTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SingleLineTextView(context, attrs, defStyleAttr) {
    fun setSeriesModel(
        mediaList: MediaList,
        isCurrentUser: Boolean,
    ) {
        val model = mediaList.media
        if (MediaUtil.isAnimeType(model)) {
            if (CompatUtil.equals(model.status, KeyUtil.NOT_YET_RELEASED)) {
                setText(R.string.TBA)
            } else {
                val total: Any = if (model.episodes < 1) "?" else model.episodes
                if (isCurrentUser && !MediaUtil.isIncrementLimitReached(mediaList)) {
                    text = String.format(Locale.getDefault(), "%s/%s +", mediaList.progress, total)
                } else {
                    text = String.format(Locale.getDefault(), "%s/%s", mediaList.progress, total)
                }
            }
        } else if (MediaUtil.isMangaType(model)) {
            if (CompatUtil.equals(model.status, KeyUtil.NOT_YET_RELEASED)) {
                setText(R.string.TBA)
            } else {
                val total: Any = if (model.chapters < 1) "?" else model.chapters
                if (isCurrentUser && !MediaUtil.isIncrementLimitReached(mediaList)) {
                    text = String.format(Locale.getDefault(), "%s/%s +", mediaList.progress, total)
                } else {
                    text = String.format(Locale.getDefault(), "%s/%s", mediaList.progress, total)
                }
            }
        }
        invalidate()
    }
}
