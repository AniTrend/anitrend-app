package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.list.FeedListFragment;

/**
 * Created by max on 2017/11/26.
 * user profile targeted feeds
 */

public class UserFeedFragment extends FeedListFragment {

    private long userId;
    private String userName;

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
            if (getArguments().containsKey(KeyUtil.arg_id))
                userId = getArguments().getLong(KeyUtil.arg_id);
            else
                userName = getArguments().getString(KeyUtil.arg_userName);
        isMenuDisabled = true; isFeed = false;
    }

    @Override
    public void makeRequest() {
        if(getPresenter().getApplicationPref().isAuthenticated() && getPresenter().isCurrentUser(userId, userName))
            userId = getPresenter().getDatabase().getCurrentUser().getId();

        if (userId > 0)
            queryContainer.putVariable(KeyUtil.arg_userId, userId);
        else
            queryContainer.putVariable(KeyUtil.arg_userName, userName);

        if (queryContainer.containsVariable(KeyUtil.arg_userId) || queryContainer.containsVariable(KeyUtil.arg_userName))
            super.makeRequest();
    }

}