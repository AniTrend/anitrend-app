package com.mxt.anitrend.util;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.mxt.anitrend.base.custom.view.widget.AutoIncrementWidget;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesManageBase;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.CustomList;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import java.util.List;
import java.util.Locale;

public class MediaListUtil {

    /**
     * Creates query variables for updating the status of the current users lists, use cases
     * @see CustomSeriesManageBase#persistChanges()
     * @see AutoIncrementWidget#updateModelState()
     *
     * @param model the current media list item
     */
    public static Bundle getMediaListParams(@NonNull MediaList model, @KeyUtil.ScoreFormat String scoreFormat) {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_scoreFormat, scoreFormat);

        if(model.getId() > 0)
            queryContainer.putVariable(KeyUtil.arg_id, model.getId());
        queryContainer.putVariable(KeyUtil.arg_mediaId, model.getMediaId());
        queryContainer.putVariable(KeyUtil.arg_listStatus, model.getStatus());
        queryContainer.putVariable(KeyUtil.arg_listScore, model.getScore());
        queryContainer.putVariable(KeyUtil.arg_listNotes, model.getNotes());
        queryContainer.putVariable(KeyUtil.arg_listPrivate, model.isHidden());
        queryContainer.putVariable(KeyUtil.arg_listPriority, model.getPriority());
        queryContainer.putVariable(KeyUtil.arg_listHiddenFromStatusLists, model.isHiddenFromStatusLists());
        queryContainer.putVariable(KeyUtil.arg_startedAt, model.getStartedAt());
        queryContainer.putVariable(KeyUtil.arg_completedAt, model.getCompletedAt());

        if(model.getAdvancedScores() != null)
            queryContainer.putVariable(KeyUtil.arg_listAdvancedScore, model.getAdvancedScores());

        if(!CompatUtil.isEmpty(model.getCustomLists())) {
            List<String> enabledCustomLists = Stream.of(model.getCustomLists())
                    .filter(CustomList::isEnabled)
                    .map(CustomList::getName)
                    .toList();
            queryContainer.putVariable(KeyUtil.arg_listCustom, enabledCustomLists);
        }

        queryContainer.putVariable(KeyUtil.arg_listRepeat, model.getRepeat());
        queryContainer.putVariable(KeyUtil.arg_listProgress, model.getProgress());
        queryContainer.putVariable(KeyUtil.arg_listProgressVolumes, model.getProgressVolumes());

        Bundle bundle = new Bundle();
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        return bundle;
    }

    /**
     * Checks if the sorting should be done on titles
     */
    public static boolean isTitleSort(@KeyUtil.MediaListSort String mediaSort) {
        return CompatUtil.equals(mediaSort, KeyUtil.TITLE);
    }

    /**
     * Checks if the current list items progress can be incremented beyond what it is currently at
     */
    public static boolean isProgressUpdatable(MediaList mediaList) {
        return mediaList.getMedia().getNextAiringEpisode() != null &&
                mediaList.getMedia().getNextAiringEpisode().getEpisode()
                        - mediaList.getProgress() >= 1;
    }

    /**
     * Filters by the given search term
     */
    public static boolean isFilterMatch(MediaList model, String filter) {
        return model.getMedia().getTitle().getEnglish().toLowerCase(Locale.getDefault()).contains(filter) ||
                model.getMedia().getTitle().getRomaji().toLowerCase(Locale.getDefault()).contains(filter) ||
                model.getMedia().getTitle().getOriginal().toLowerCase(Locale.getDefault()).contains(filter);
    }
}
