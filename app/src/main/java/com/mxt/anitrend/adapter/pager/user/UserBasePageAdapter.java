package com.mxt.anitrend.adapter.pager.user;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.detail.fragment.UserAboutFragment;
import com.mxt.anitrend.view.detail.fragment.UserProgressFragment;
import com.mxt.anitrend.view.detail.fragment.UserStatusFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

public class UserBasePageAdapter extends DefaultStatePagerAdapter {

    private User mUser;

    public UserBasePageAdapter(FragmentManager fragmentManager, User model, Context context) {
        super(fragmentManager, context);
        mUser = model;
        mTitles = context.getResources().getStringArray(R.array.profile_page_titles);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserAboutFragment.newInstance(mUser);
            case 1:
                return UserProgressFragment.newInstance(KeyUtils.ActivtyTypes[KeyUtils.PROGRESS], mUser.getDisplay_name());
            case 2:
                return UserStatusFragment.newInstance(KeyUtils.ActivtyTypes[KeyUtils.STATUS], mUser.getDisplay_name());
        }
        return null;
    }
}