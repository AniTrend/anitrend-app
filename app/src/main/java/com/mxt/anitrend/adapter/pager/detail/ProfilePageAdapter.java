package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.detail.UserAboutFragment;
import com.mxt.anitrend.view.fragment.detail.UserFeedFragment;

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
                return UserAboutFragment.newInstance();
            case 1:
                return UserFeedFragment.newInstance(KeyUtils.PROGRESS);
            case 2:
                return UserFeedFragment.newInstance(KeyUtils.STATUS);
            case 3:
                return UserFeedFragment.newInstance(KeyUtils.MESSAGE);
        }
        return null;
    }
}
