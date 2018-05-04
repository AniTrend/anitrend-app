package com.mxt.anitrend.util;


import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.CustomList;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

import java.util.List;

public class MediaListUtil {

    public static Bundle getMediaListParams(@NonNull MediaList model) {
        QueryContainerBuilder queryContainer = GraphUtil.getDefaultQuery(false);

        if(model.getId() > 0)
            queryContainer.putVariable(KeyUtil.arg_id, model.getId());
        queryContainer.putVariable(KeyUtil.arg_mediaId, model.getMediaId());
        queryContainer.putVariable(KeyUtil.arg_listStatus, model.getStatus());
        queryContainer.putVariable(KeyUtil.arg_listScore_raw, model.getScore());
        queryContainer.putVariable(KeyUtil.arg_listNotes, model.getNotes());
        queryContainer.putVariable(KeyUtil.arg_listPrivate, model.isHidden());
        queryContainer.putVariable(KeyUtil.arg_listPriority, model.getPriority());
        queryContainer.putVariable(KeyUtil.arg_listHiddenFromStatusLists, model.isHiddenFromStatusLists());

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
}
