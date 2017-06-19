package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.view.base.fragment.AnimeSearchFragment;
import com.mxt.anitrend.view.base.fragment.MangaSearchFragment;
import com.mxt.anitrend.view.base.fragment.StudioSearchFragment;
import com.mxt.anitrend.view.base.fragment.UserSearchFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by Maxwell on 11/6/2016.
 */

public class SearchPageAdapter extends DefaultStatePagerAdapter {

    private final String query;

    public SearchPageAdapter(FragmentManager fm, String query, Context mContext) {
        super(fm);
        this.query = query;
        mPages = new ApplicationPrefs(mContext).isAuthenticated()?4:4-1;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return AnimeSearchFragment.newInstance(query);
            case 1:
                return MangaSearchFragment.newInstance(query);
            case 2:
                return StudioSearchFragment.newInstance(query);
            case 3:
                return UserSearchFragment.newInstance(query);
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
