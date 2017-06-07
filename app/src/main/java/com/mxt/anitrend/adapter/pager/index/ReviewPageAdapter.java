package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.ReviewFragment;

import java.util.Locale;

/**
 * Created by max on 2017/05/02.
 */

public class ReviewPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 4;

    public ReviewPageAdapter(FragmentManager manager) {
        super(manager);
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
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return pages;
    }
}