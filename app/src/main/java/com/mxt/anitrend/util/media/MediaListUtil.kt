package com.mxt.anitrend.util.media

import android.os.Bundle
import com.mxt.anitrend.base.custom.view.widget.AutoIncrementWidget
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import java.util.*

object MediaListUtil {

    /**
     * Creates query variables for updating the status of the current users lists, use cases
     * @see AutoIncrementWidget.updateModelState
     * @see BottomSheetSeriesManage.handleSave
     * @param model the current media list item
     */
    fun getMediaListParams(model: MediaList, @KeyUtil.ScoreFormat scoreFormat: String): Bundle = Bundle().apply {
        putString(KeyUtil.arg_scoreFormat, scoreFormat)
        if (model.id > 0) {
            putLong(KeyUtil.arg_id, model.id)
        }
        putLong(KeyUtil.arg_mediaId, model.mediaId)
        putString(KeyUtil.arg_listStatus, model.status)
        putDouble(KeyUtil.arg_listScore, model.score.toDouble())
        model.scoreRaw?.let { putInt(KeyUtil.arg_listScore_raw, it) }
        putString(KeyUtil.arg_listNotes, model.notes)
        putBoolean(KeyUtil.arg_listPrivate, model.isHidden)
        putInt(KeyUtil.arg_listPriority, model.priority)
        putBoolean(KeyUtil.arg_listHiddenFromStatusLists, model.isHiddenFromStatusLists)
        putParcelable(KeyUtil.arg_startedAt, model.startedAt)
        putParcelable(KeyUtil.arg_completedAt, model.completedAt)
        model.advancedScores?.let {
            putSerializable(
                KeyUtil.arg_listAdvancedScore,
                ArrayList(it.values.map { score -> score.toDouble() }),
            )
        }

        val customLists = model.customLists.orEmpty()
        if (!CompatUtil.isEmpty(customLists)) {
            val enabledCustomLists = customLists
                .filter { it.isEnabled }
                .map { it.name.orEmpty() }
                .filter { it.isNotEmpty() }
            putStringArrayList(KeyUtil.arg_listCustom, ArrayList(enabledCustomLists))
        }

        putInt(KeyUtil.arg_listRepeat, model.repeat)
        putInt(KeyUtil.arg_listProgress, model.progress)
        putInt(KeyUtil.arg_listProgressVolumes, model.progressVolumes)
    }

    /**
     * Checks if the sorting should be done on titles
     */
    fun isTitleSort(@KeyUtil.MediaListSort mediaSort: String): Boolean = CompatUtil.equals(
        mediaSort,
        KeyUtil.TITLE,
    )

    /**
     * Checks if the current list items progress can be incremented beyond what it is currently at
     */
    fun isProgressUpdatable(mediaList: MediaList): Boolean {
        val nextEpisode = mediaList.media.nextAiringEpisode ?: return false
        return nextEpisode.episode - mediaList.progress >= 1
    }

    /**
     * Filters by the given search term
     */
    fun isFilterMatch(model: MediaList, filter: String): Boolean = model.media.title?.english?.lowercase(Locale.getDefault())?.contains(filter) == true ||
        model.media.title?.romaji?.lowercase(Locale.getDefault())?.contains(filter) == true ||
        model.media.title?.original?.lowercase(Locale.getDefault())?.contains(filter) == true
}
