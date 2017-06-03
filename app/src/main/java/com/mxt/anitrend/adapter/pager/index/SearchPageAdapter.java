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

import java.util.Locale;

/**
 * Created by Maxwell on 11/6/2016.
 */

public class SearchPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 4;
    private final String query;
    private ApplicationPrefs mPrefs;

    public SearchPageAdapter(FragmentManager fm, String query, Context mContext) {
        super(fm);
        this.query = query;
        this.mPrefs = new ApplicationPrefs(mContext);
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

    @Override
    public int getCount() {
        return mPrefs.isAuthenticated()?pages:pages-1;
    }
}
