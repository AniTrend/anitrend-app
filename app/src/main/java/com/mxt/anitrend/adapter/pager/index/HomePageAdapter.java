package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.ProgressFragment;
import com.mxt.anitrend.view.index.fragment.PublicStatusFragment;
import com.mxt.anitrend.view.index.fragment.StatusFragment;

import java.util.Locale;

/**
 * Created by max on 2017/03/09.
 */

public class HomePageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 3;

    public HomePageAdapter(FragmentManager fm) {
        super(fm);
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
                return ProgressFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PROGRESS.ordinal()]);
            case 1:
                return StatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.STATUS.ordinal()]);
            case 2:
                return PublicStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PUBIC_STATUS.ordinal()]);
        }
        return null;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return pages;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position)
        {
            case 0:
                return "progress".toUpperCase(locale);
            case 1:
                return "status".toUpperCase(locale);
            case 3:
                return "public status".toUpperCase(locale);
        }
        return null;
    }
}
