package com.mxt.anitrend.adapter.pager.details;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.view.detail.fragment.FavouritesFragment;

import java.util.Locale;

/**
 * Created by max on 2017/05/16.
 */

public class FavouritesPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 5;
    private Favourite model;

    public FavouritesPageAdapter(FragmentManager manager, Favourite model) {
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
                return FavouritesFragment.newInstance(model.getAnime(), 0);
            case 1:
                return FavouritesFragment.newInstance(model.getCharacter(), 1);
            case 2:
                return FavouritesFragment.newInstance(model.getManga(), 2);
            case 3:
                return FavouritesFragment.newInstance(model.getStaff(), 3);
            case 4:
                return FavouritesFragment.newInstance(model.getStudio(), 4);
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
                return "anime".toUpperCase(locale);
            case 1:
                return "characters".toUpperCase(locale);
            case 2:
                return "manga".toUpperCase(locale);
            case 3:
                return "staff".toUpperCase(locale);
            case 4:
                return "studios".toUpperCase(locale);
        }
        return null;
    }
}
