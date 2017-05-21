package com.mxt.anitrend.adapter.pager.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.detail.fragment.UserAboutFragment;
import com.mxt.anitrend.view.detail.fragment.UserProgressFragment;
import com.mxt.anitrend.view.detail.fragment.UserStatusFragment;

import java.util.Locale;

public class UserBasePageAdapter extends FragmentStatePagerAdapter {

    private User user;

    public UserBasePageAdapter(FragmentManager fm, User model) {
        super(fm);
        this.user = model;
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
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position) {
            case 0:
                return "overview".toUpperCase(locale);
            case 1:
                return "progress".toUpperCase(locale);
            case 2:
                return "status".toUpperCase(locale);
        }
        return null;
    }
}