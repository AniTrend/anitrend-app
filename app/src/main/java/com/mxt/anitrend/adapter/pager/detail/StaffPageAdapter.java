package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.extension.KoinExt;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.view.fragment.detail.StaffOverviewFragment;
import com.mxt.anitrend.view.fragment.group.MediaAnimeRoleFragment;
import com.mxt.anitrend.view.fragment.group.MediaFormatFragment;
import com.mxt.anitrend.view.fragment.group.MediaStaffRoleFragment;

/**
 * Created by max on 2017/12/01.
 */

public class StaffPageAdapter extends BaseStatePageAdapter {

    public StaffPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.staff_page_titles);
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        boolean isAuthenticated = KoinExt.get(Settings.class).isAuthenticated();

        switch (position) {
            case 0:
                return StaffOverviewFragment.newInstance(getParams());
            case 1: {
                MediaAnimeRoleFragment fragment = MediaAnimeRoleFragment.newInstance(getParams(), KeyUtil.ANIME, KeyUtil.STAFF_CHARACTERS_REQ);
                fragment.setFilterable(isAuthenticated);
                return fragment;
            }
            case 2: {
                MediaFormatFragment fragment = MediaFormatFragment.newInstance(getParams(), KeyUtil.MANGA, KeyUtil.STAFF_MEDIA_REQ);
                fragment.setFilterable(isAuthenticated);
                return fragment;
            }
            case 3: {
                MediaStaffRoleFragment fragment = MediaStaffRoleFragment.newInstance(getParams());
                fragment.setFilterable(isAuthenticated);
                return fragment;
            }
        }
        return null;
    }
}