package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.graphql.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.list.FeedListFragment;

/**
 * Created by max on 2017/11/07.
 */

public class FeedPageAdapter extends BaseStatePageAdapter {

    public FeedPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.feed_titles);
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return FeedListFragment.newInstance(getParams(), GraphUtil.INSTANCE.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_isFollowing, true)
                        .putVariable(KeyUtil.arg_type, KeyUtil.MEDIA_LIST));
            case 1:
                return FeedListFragment.newInstance(getParams(), GraphUtil.INSTANCE.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_isFollowing, true)
                        .putVariable(KeyUtil.arg_type, KeyUtil.TEXT)
                        .putVariable(KeyUtil.arg_asHtml, false));
            case 2:
                return FeedListFragment.newInstance(getParams(), GraphUtil.INSTANCE.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_isFollowing, false)
                        .putVariable(KeyUtil.arg_isMixed, true)
                        .putVariable(KeyUtil.arg_asHtml, false));
        }
        return null;
    }
}
