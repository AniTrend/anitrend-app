package com.mxt.anitrend.adapter.pager.details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.model.Staff;
import com.mxt.anitrend.view.detail.fragment.StaffAnimeFragment;
import com.mxt.anitrend.view.detail.fragment.StaffMangaFragment;
import com.mxt.anitrend.view.detail.fragment.StaffOverviewFragment;

import java.util.Locale;

/**
 * Created by max on 2017/04/09.
 * This class should dynamically decide which pages to display
 */
public class StaffPageAdapter extends FragmentStatePagerAdapter {

    private Staff mStaff;

    public StaffPageAdapter(FragmentManager fm, Staff mStaff) {
        super(fm);
        this.mStaff = mStaff;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return StaffOverviewFragment.newInstance(mStaff);
            case 1:
                return StaffAnimeFragment.newInstance(mStaff);
            case 2:
                return StaffMangaFragment.newInstance(mStaff);
            case 3:
                return StaffOverviewFragment.newInstance(mStaff);
        }
        return null;
    }

    @Override
    public int getCount() {
        /*return mStaff.getAnime() != null &&
                mStaff.getAnime().size() > 0 ? 3 : 2;*/
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position) {
            case 0:
                return "overview".toUpperCase(locale);
            case 1:
                return "anime roles".toUpperCase(locale);
            case 2:
                return "manga roles".toUpperCase(locale);
            case 3:
                return "staff roles".toUpperCase(locale);
        }
        return null;
    }
}
