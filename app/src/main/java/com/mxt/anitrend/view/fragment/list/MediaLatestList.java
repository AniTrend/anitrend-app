package com.mxt.anitrend.view.fragment.list;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;

public class MediaLatestList extends MediaBrowseFragment {

    public static MediaLatestList newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        return (MediaLatestList) newInstance(params, queryContainer, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFilterable = false;
    }
}
