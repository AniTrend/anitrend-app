package com.mxt.anitrend.adapter.pager.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.detail.fragment.UserAboutFragment;
import com.mxt.anitrend.view.detail.fragment.UserProgressFragment;
import com.mxt.anitrend.view.detail.fragment.UserStatusFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/04/03.
 */
public class UserPageAdapter extends DefaultStatePagerAdapter {

    private User user;

    public UserPageAdapter(FragmentManager fm, User model, String[] titles) {
        super(fm);
        this.user = model;
        mTitles = titles;
        mPages = 3;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserAboutFragment.newInstance(user);
            case 1:
                return UserProgressFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PROGRESS.ordinal()], user.getDisplay_name());
            case 2:
                return UserStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.STATUS.ordinal()], user.getDisplay_name());
            /*case 3:
                return UserStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.MESSAGE.ordinal()], user.getDisplay_name());*/
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        return mTitles[position].toUpperCase(locale);
    }
}