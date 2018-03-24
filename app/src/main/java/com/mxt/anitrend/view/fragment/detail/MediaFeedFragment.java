package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.index.FeedFragment;

/**
 * Created by max on 2018/03/24.
 */

public class MediaFeedFragment extends FeedFragment {

    public static MediaFeedFragment newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtils.arg_graph_params, queryContainer);
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
        queryContainer.putVariable(KeyUtils.arg_page, getPresenter().getCurrentPage());
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtils.MEDIA_SOCIAL_REQ, getContext());
    }
}
