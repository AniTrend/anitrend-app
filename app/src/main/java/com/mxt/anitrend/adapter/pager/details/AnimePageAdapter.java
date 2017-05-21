package com.mxt.anitrend.adapter.pager.details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.view.detail.fragment.AnimeExtrasFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeLinksFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeOverviewFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeReviewsFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeWatchFragment;

import java.util.Locale;

/**
 * Created by Maxwell on 10/17/2016.
 */

public class AnimePageAdapter extends FragmentPagerAdapter {

    private static final int pages = 5;
    private Series model;

    public AnimePageAdapter(FragmentManager manager, Series model) {
        super(manager);
        this.model = model;
    }

    @Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
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
                return AnimeOverviewFragment.newInstance(model);
            case 1:
                return AnimeLinksFragment.newInstance(model);
            case 2:
                return AnimeWatchFragment.newInstance(model.getExternal_links());
            case 3:
                return AnimeExtrasFragment.newInstance(model);
            case 4:
                return AnimeReviewsFragment.newInstance(model.getId());
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

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     *
     * @return A title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        switch (position) {
            case 0:
                return "info".toUpperCase(locale);
            case 1:
                return "links".toUpperCase(locale);
            case 2:
                return "episodes".toUpperCase(locale);
            case 3:
                return "extras".toUpperCase(locale);
            case 4:
                return "reviews".toUpperCase(locale);
        }
        return null;
    }
}