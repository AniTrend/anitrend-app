package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.SeasonFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by Maxwell on 10/14/2016.
 */
public class SeasonPageAdapter extends DefaultStatePagerAdapter {

    public SeasonPageAdapter(FragmentManager manager) {
        super(manager);
        mPages = 4;
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

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
