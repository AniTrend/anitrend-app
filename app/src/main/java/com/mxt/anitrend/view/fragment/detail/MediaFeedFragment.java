package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.list.FeedListFragment;

/**
 * Created by max on 2018/03/24.
 */

public class MediaFeedFragment extends FeedListFragment {

    public static MediaFeedFragment newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        MediaFeedFragment fragment = new MediaFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isMenuDisabled = true; isFeed = false;
    }

    @Override
    public void makeRequest() {
        queryContainer.putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.MEDIA_SOCIAL_REQ, getContext());
    }
}
