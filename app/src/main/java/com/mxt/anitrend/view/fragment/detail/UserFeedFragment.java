package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.base.interfaces.event.PublisherListener;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.list.FeedListFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by max on 2017/11/26.
 * user profile targeted feeds
 */

public class UserFeedFragment extends FeedListFragment {

    public static UserFeedFragment newInstance(Bundle params, QueryContainerBuilder queryContainer) {
        Bundle args = new Bundle(params);
        args.putParcelable(KeyUtil.arg_graph_params, queryContainer);
        UserFeedFragment fragment = new UserFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Override and set presenter, mColumnSize, and fetch argument/s
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            queryContainer.putVariable(KeyUtil.arg_userId, getArguments().getLong(KeyUtil.arg_id))
                    .putVariable(KeyUtil.arg_userName, getArguments().getString(KeyUtil.arg_userName));
        isMenuDisabled = true; isFeed = false;
    }

    @Override
    public void makeRequest() {
        if (queryContainer.containsVariable(KeyUtil.arg_userId) || queryContainer.containsVariable(KeyUtil.arg_userName))
            super.makeRequest();
    }

}