package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.ReviewFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/05/02.
 */

public class ReviewPageAdapter extends DefaultStatePagerAdapter {

    public ReviewPageAdapter(FragmentManager manager) {
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
        switch (position) {
            case 0:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.LATEST.ordinal());
            case 1:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.POPULAR.ordinal());
            case 2:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.NEED_LOVE.ordinal());
            case 3:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.CONTROVERSIAL.ordinal());
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