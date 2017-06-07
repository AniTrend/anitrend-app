package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.view.index.fragment.SuggestionFragment;

import java.util.Locale;

/**
 * Created by max on 2017/05/02.
 */
public class HubPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 1;

    public HubPageAdapter(FragmentManager manager) {
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
                return SuggestionFragment.newInstance();
            case 1:
                return null;
            case 2:
                return null;
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