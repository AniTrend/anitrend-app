package com.mxt.anitrend.adapter.pager.details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.view.detail.fragment.MangaExtrasFragment;
import com.mxt.anitrend.view.detail.fragment.MangaLinksFragment;
import com.mxt.anitrend.view.detail.fragment.MangaOverviewFragment;
import com.mxt.anitrend.view.detail.fragment.MangaReviewFragment;

import java.util.Locale;

/**
 * Created by Maxwell on 11/3/2016.
 */
public class MangaPageAdapter extends FragmentPagerAdapter {

    private static final int pages = 4;
    private Series model;

    public MangaPageAdapter(FragmentManager manager, Series model) {
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
                return MangaOverviewFragment.newInstance(model);
            case 1:
                return MangaLinksFragment.newInstance(model);
            case 2:
                return MangaExtrasFragment.newInstance(model);
            case 3:
                return MangaReviewFragment.newInstance(model.getId());
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
                return "Info".toUpperCase(locale);
            case 1:
                return "Stats".toUpperCase(locale);
            case 2:
                return "Extras".toUpperCase(locale);
            case 3:
                return "Reviews".toUpperCase(locale);
        }
        return null;
    }
}