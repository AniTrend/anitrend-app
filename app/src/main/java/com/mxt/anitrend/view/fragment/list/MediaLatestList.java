package com.mxt.anitrend.view.fragment.list;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.mxt.anitrend.util.KeyUtil;

import io.github.wax911.library.model.request.QueryContainerBuilder;

public class MediaLatestList extends MediaBrowseFragment {

    public static MediaLatestList newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        MediaLatestList fragment = new MediaLatestList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFilterable = false;
    }

    @Override
    public void makeRequest() {
        Bundle bundle = getViewModel().getParams();
        queryContainer.putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        bundle.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_BROWSE_REQ, getContext());
    }
}
