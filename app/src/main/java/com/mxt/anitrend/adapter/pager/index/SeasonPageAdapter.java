package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.SeasonFragment;

import java.util.Locale;

/**
 * Created by Maxwell on 10/14/2016.
 */
public class SeasonPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 4;

    public SeasonPageAdapter(FragmentManager manager) {
        super(manager);
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
                return SeasonFragment.newInstance(FilterTypes.SeasonTitle.WINTER.ordinal());
            case 1:
                return SeasonFragment.newInstance(FilterTypes.SeasonTitle.SPRING.ordinal());
            case 2:
                return SeasonFragment.newInstance(FilterTypes.SeasonTitle.SUMMER.ordinal());
            case 3:
                return SeasonFragment.newInstance(FilterTypes.SeasonTitle.FALL.ordinal());
        }
        return null;
    }

    @Override
    public int getCount() {
        return pages;
    }
}
