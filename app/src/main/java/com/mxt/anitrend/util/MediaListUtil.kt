package com.mxt.anitrend.util

import android.os.Bundle

import com.annimon.stream.Stream
import com.mxt.anitrend.base.custom.view.widget.AutoIncrementWidget
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesManageBase
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.util.graphql.GraphUtil
import java.util.Locale

object MediaListUtil {

    /**
     * Creates query variables for updating the status of the current users lists, use cases
     * @see CustomSeriesManageBase.persistChanges
     * @see AutoIncrementWidget.updateModelState
     * @param model the current media list item
     */
    fun getMediaListParams(model: MediaList, @KeyUtil.ScoreFormat scoreFormat: String): Bundle {
        val queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_scoreFormat, scoreFormat)

        if (model.id > 0)
            queryContainer.putVariable(KeyUtil.arg_id, model.id)
        queryContainer.putVariable(KeyUtil.arg_mediaId, model.mediaId)
        queryContainer.putVariable(KeyUtil.arg_listStatus, model.status)
        queryContainer.putVariable(KeyUtil.arg_listScore, model.score)
        queryContainer.putVariable(KeyUtil.arg_listNotes, model.notes)
        queryContainer.putVariable(KeyUtil.arg_listPrivate, model.isHidden)
        queryContainer.putVariable(KeyUtil.arg_listPriority, model.priority)
        queryContainer.putVariable(KeyUtil.arg_listHiddenFromStatusLists, model.isHiddenFromStatusLists)
        queryContainer.putVariable(KeyUtil.arg_startedAt, model.startedAt)
        queryContainer.putVariable(KeyUtil.arg_completedAt, model.completedAt)

        if (model.advancedScores != null)
            queryContainer.putVariable(KeyUtil.arg_listAdvancedScore, model.advancedScores)

        if (!CompatUtil.isEmpty(model.customLists)) {
            val enabledCustomLists = Stream.of(model.customLists)
                    .filter { it.isEnabled }
                    .map { it.name }
                    .toList()
            queryContainer.putVariable(KeyUtil.arg_listCustom, enabledCustomLists)
        }

        queryContainer.putVariable(KeyUtil.arg_listRepeat, model.repeat)
        queryContainer.putVariable(KeyUtil.arg_listProgress, model.progress)
        queryContainer.putVariable(KeyUtil.arg_listProgressVolumes, model.progressVolumes)

        val bundle = Bundle()
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer)
        return bundle
    }

    /**
     * Checks if the sorting should be done on titles
     */
    fun isTitleSort(@KeyUtil.MediaListSort mediaSort: String): Boolean {
        return CompatUtil.equals(mediaSort, KeyUtil.TITLE)
    }

    /**
     * Checks if the current list items progress can be incremented beyond what it is currently at
     */
    fun isProgressUpdatable(mediaList: MediaList): Boolean {
        return mediaList.media.nextAiringEpisode != null && mediaList.media.nextAiringEpisode!!.episode - mediaList.progress >= 1
    }

    /**
     * Filters by the given search term
     */
    fun isFilterMatch(model: MediaList, filter: String): Boolean {
        return model.media.title.english.toLowerCase(Locale.getDefault()).contains(filter) ||
                model.media.title.romaji.toLowerCase(Locale.getDefault()).contains(filter) ||
                model.media.title.original.toLowerCase(Locale.getDefault()).contains(filter)
    }
}
