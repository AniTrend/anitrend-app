package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
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
        switch (position) {
            case 0:
                return MessageFeedFragment.newInstance(getParams(), KeyUtil.MESSAGE_TYPE_INBOX);
            case 1:
                return MessageFeedFragment.newInstance(getParams(), KeyUtil.MESSAGE_TYPE_OUTBOX);
        }
        return null;
    }
}
