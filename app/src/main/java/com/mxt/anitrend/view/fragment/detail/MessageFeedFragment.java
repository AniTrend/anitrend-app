package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.index.FeedFragment;

/**
 * Created by max on 2018/03/24.
 * MessageFeedFragment
 */

public class MessageFeedFragment extends FeedFragment {

    private long userId;
    private @KeyUtils.MessageType int messageType;

    public static MessageFeedFragment newInstance(Bundle params, @KeyUtils.MessageType int messageType) {
        Bundle args = new Bundle(params);
        args.putInt(KeyUtils.arg_message_type, messageType);
        MessageFeedFragment fragment = new MessageFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            messageType = getArguments().getInt(KeyUtils.arg_message_type);
            userId = getArguments().getLong(KeyUtils.arg_userId);
        }
        isMenuDisabled = true; isFeed = false;
    }

    @Override
    public void makeRequest() {
        queryContainer.putVariable(KeyUtils.arg_page, getPresenter().getCurrentPage())
                .putVariable(messageType == KeyUtils.MESSAGE_TYPE_INBOX ? KeyUtils.arg_userId : KeyUtils.arg_messengerId, userId);
        getViewModel().getParams().putParcelable(KeyUtils.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtils.FEED_MESSAGE_REQ, getContext());
    }
}
