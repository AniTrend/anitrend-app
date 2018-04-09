package com.mxt.anitrend.view.fragment.list;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.util.KeyUtil;

public class MediaLatestList extends MediaBrowseFragment {

    public static MediaLatestList newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        args.putBoolean(KeyUtil.arg_media_compact, false);
        MediaLatestList fragment = new MediaLatestList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFilterable = false;
    }
}
