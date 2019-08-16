package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.detail.UserFeedFragment;
import com.mxt.anitrend.view.fragment.detail.UserOverviewFragment;

/**
 * Created by max on 2017/11/16.
 */

public class ProfilePageAdapter extends BaseStatePageAdapter {

    public ProfilePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.profile_page_titles);
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserOverviewFragment.newInstance(getParams());
            case 1:
                return UserFeedFragment.newInstance(getParams(), GraphUtil.INSTANCE.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_type, KeyUtil.MEDIA_LIST));
            case 2:
                return UserFeedFragment.newInstance(getParams(), GraphUtil.INSTANCE.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_type, KeyUtil.TEXT));
        }
        return null;
    }
}
