package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.view.index.fragment.NewAnimeFragment;
import com.mxt.anitrend.view.index.fragment.TrendingFragment;

import java.util.Locale;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class TrendingPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 2;

    public TrendingPageAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return TrendingFragment.newInstance();
            case 1:
                return NewAnimeFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return pages;
    }
}