package com.mxt.anitrend.adapter.pager.details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.model.Staff;
import com.mxt.anitrend.view.detail.fragment.StaffAnimeFragment;
import com.mxt.anitrend.view.detail.fragment.StaffMangaFragment;
import com.mxt.anitrend.view.detail.fragment.StaffOverviewFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/04/09.
 * This class should dynamically decide which pages to display
 */
public class StaffPageAdapter extends DefaultStatePagerAdapter {

    private Staff model;

    public StaffPageAdapter(FragmentManager manager, Staff model, String[] titles) {
        super(manager);
        this.model = model;
        mTitles = titles;
        mPages = getPages();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return StaffOverviewFragment.newInstance(model);
            case 1:
                return StaffAnimeFragment.newInstance(model);
            case 2:
                return StaffMangaFragment.newInstance(model);
            case 3:
                return StaffOverviewFragment.newInstance(model);
        }
        return null;
    }

    private int getPages() {
        return model != null && model.getAnime() != null && model.getAnime().size() > 0? 3 : 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        return mTitles[position].toUpperCase(locale);
    }
}
