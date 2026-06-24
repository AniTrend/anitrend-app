package com.mxt.anitrend.util.media

import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.MediaTrend
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.KeyUtil
import java.util.Locale

/**
 * Created by max on 2018/02/23.
 * Helper class to handle series types
 */
object MediaUtil {
    fun <T : MediaBase> isAnimeType(series: T?): Boolean = series?.type == KeyUtil.ANIME

    fun <T : MediaBase> isMangaType(series: T?): Boolean = series?.type == KeyUtil.MANGA

    fun isIncrementLimitReached(model: MediaList): Boolean {
        val mediaBase = model.media
        return if (isAnimeType(mediaBase)) {
            mediaBase.episodes == model.progress && mediaBase.episodes != 0
        } else {
            mediaBase.chapters == model.progress && mediaBase.chapters != 0
        }
    }

    fun isAllowedStatus(model: MediaList): Boolean {
        val mediaBase = model.media
        return mediaBase.status != KeyUtil.NOT_YET_RELEASED
    }

    fun <T : MediaBase> getMediaTitle(series: T): String = series.title?.userPreferred.orEmpty()

    fun getMediaListTitle(mediaList: MediaList): String = getMediaTitle(mediaList.media)

    fun mapMediaTrend(mediaTrends: List<MediaTrend>?): List<MediaBase> = mediaTrends?.mapNotNull { it.media } ?: emptyList()

    fun getAiringMedia(mediaLists: List<MediaList>?): List<MediaList> = mediaLists?.filter {
        it.media.status == KeyUtil.RELEASING
    } ?: emptyList()

    fun getFormattedCount(amount: Int): String = when {
        amount >= 1000 ->
            String.format(Locale.getDefault(), "%.1f K", amount.toFloat() / 1000)
        amount < 1 -> "?"
        else -> String.format(Locale.getDefault(), "%d", amount)
    }
}
