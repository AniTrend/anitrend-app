package com.mxt.anitrend.presenter.fragment

import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.View
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.Media
import com.mxt.anitrend.model.entity.anilist.meta.ScoreDistribution
import com.mxt.anitrend.model.entity.anilist.meta.StatusDistribution
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.util.media.MediaUtil
import java.util.Locale

/**
 * Created by max on 2018/01/01.
 */
class MediaPresenter(
    context: Context,
) : BasePresenter(context) {
    @Suppress("DEPRECATION")
    fun getHashTag(media: Media?): Spanned {
        val hashTag = media?.hashTag
        return if (!hashTag.isNullOrEmpty()) {
            Html.fromHtml(
                String.format(
                    "<a href=\"https://twitter.com/search?q=%%23%s&src=typd\">%s</a>",
                    hashTag.replace("#", ""),
                    hashTag,
                ),
            )
        } else {
            val ctx = context
            Html.fromHtml(ctx.getString(R.string.TBA))
        }
    }

    fun getMainStudio(media: Media?): String {
        val ctx = context
        val studioContainer: ConnectionContainer<List<StudioBase>>? = media?.studios
        return studioContainer?.connection?.firstOrNull()?.name
            ?: ctx.getString(R.string.TBA)
    }

    fun getMainStudioObject(media: Media?): StudioBase? {
        val studioContainer: ConnectionContainer<List<StudioBase>>? = media?.studios
        return studioContainer?.connection?.firstOrNull()
    }

    @Suppress("DEPRECATION")
    fun getMediaStats(statusDistribution: List<StatusDistribution>): List<PieEntry> {
        val highestStatus = statusDistribution.maxOfOrNull { it.amount } ?: 0
        if (highestStatus > 0) {
            return statusDistribution
                .map { status ->
                    PieEntry(
                        (status.amount * 100f) / highestStatus,
                        String.format(
                            Locale.getDefault(),
                            "%s: %s",
                            CompatUtil.capitalizeWords(status.status),
                            MediaUtil.getFormattedCount(status.amount),
                        ),
                    )
                }.sortedBy { it.label }
        }
        return emptyList()
    }

    fun getMediaScoreDistribution(scoreDistribution: List<ScoreDistribution>): List<BarEntry> = scoreDistribution.mapIndexed { index, score ->
        BarEntry(index.toFloat(), score.amount.toFloat())
    }

    fun getEpisodeDuration(media: Media?): String {
        val ctx = context
        return if (media?.duration != null && media.duration > 0) {
            ctx.getString(R.string.text_anime_length, media.duration)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun getMediaSeason(media: Media?): String {
        val ctx = context
        val startDate = media?.startDate
        return if (startDate?.isValidDate == true) {
            DateUtil.getMediaSeason(startDate)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun getMediaSource(media: Media?): String {
        val ctx = context
        return if (!media?.source.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(media.source)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun getMediaStatus(media: Media?): String {
        val ctx = context
        return if (!media?.status.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(media.status)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun getEpisodeCount(media: Media?): String {
        val ctx = context
        return if (media?.episodes != null && media.episodes > 0) {
            ctx.getString(R.string.text_anime_episodes, media.episodes)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun getVolumeCount(media: Media?): String {
        val ctx = context
        return if (media?.volumes != null && media.volumes > 0) {
            ctx.getString(R.string.text_manga_volumes, media.volumes)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun getChapterCount(media: Media?): String {
        val ctx = context
        return if (media?.chapters != null && media.chapters > 0) {
            ctx.getString(R.string.text_manga_chapters, media.chapters)
        } else {
            ctx.getString(R.string.TBA)
        }
    }

    fun buildGenres(media: Media?): List<Genre> = media
        ?.genres
        .orEmpty()
        .takeWhile { it.isNotEmpty() }
        .map { Genre(it) }

    fun getMediaFormat(media: MediaBase?): String {
        val ctx = context
        return if (!media?.format.isNullOrEmpty()) {
            CompatUtil.capitalizeWords(media.format)
        } else {
            ctx.getString(R.string.tba_placeholder)
        }
    }

    fun getMediaScore(media: Media?): String {
        val ctx = context
        return if (media != null) {
            ctx.getString(R.string.text_anime_score, media.meanScore)
        } else {
            ctx.getString(R.string.tba_placeholder)
        }
    }

    fun isAnime(media: Media?): Int = if (MediaUtil.isAnimeType(media)) View.VISIBLE else View.GONE

    fun isManga(media: Media?): Int = if (MediaUtil.isMangaType(media)) View.VISIBLE else View.GONE
}
