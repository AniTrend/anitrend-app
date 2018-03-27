package com.mxt.anitrend.view.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.index.FeedFragment;

/**
 * Created by max on 2018/03/24.
 * MessageFeedFragment
 */

public class MessageFeedFragment extends FeedFragment {

    private long userId;
    private @KeyUtil.MessageType int messageType;

    public static MessageFeedFragment newInstance(Bundle params, @KeyUtil.MessageType int messageType) {
        Bundle args = new Bundle(params);
        args.putInt(KeyUtil.arg_message_type, messageType);
        MessageFeedFragment fragment = new MessageFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            messageType = getArguments().getInt(KeyUtil.arg_message_type);
            userId = getArguments().getLong(KeyUtil.arg_userId);
        }
        isMenuDisabled = true; isFeed = false;
    }

    @Override
    public void makeRequest() {
        queryContainer.putVariable(KeyUtil.arg_page, getPresenter().getCurrentPage())
                .putVariable(messageType == KeyUtil.MESSAGE_TYPE_INBOX ? KeyUtil.arg_userId : KeyUtil.arg_messengerId, userId);
        getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, queryContainer);
        getViewModel().requestData(KeyUtil.FEED_MESSAGE_REQ, getContext());
    }
}
