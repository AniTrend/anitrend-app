package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.detail.MessageFeedFragment;

/**
 * Created by max on 2018/03/24.
 */

public class MessagePageAdapter extends BaseStatePageAdapter {

    public MessagePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.messages_page_titles);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle(getParams());
        switch (position) {
            case 0:
                args.putParcelable(KeyUtils.arg_graph_params, GraphUtil.getDefaultQuery(true));
                return MessageFeedFragment.newInstance(getParams(), KeyUtils.MESSAGE_TYPE_INBOX);
            case 1:
                args.putParcelable(KeyUtils.arg_graph_params, GraphUtil.getDefaultQuery(true));
                return MessageFeedFragment.newInstance(getParams(), KeyUtils.MESSAGE_TYPE_OUTBOX);
        }
        return null;
    }
}
