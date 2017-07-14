package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.view.base.fragment.AnimeSearchFragment;
import com.mxt.anitrend.view.base.fragment.CharacterSearchFragment;
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

    public SearchPageAdapter(FragmentManager fragmentManager, String query, Context context) {
        super(fragmentManager, context);
        this.query = query;
        boolean isAuth = new ApplicationPrefs(context).isAuthenticated();
        mTitles = context.getResources().getStringArray(isAuth ? R.array.search_titles_auth : R.array.search_titles);
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
                return CharacterSearchFragment.newInstance(query);
            case 4:
                return UserSearchFragment.newInstance(query);
        }
        return null;
    }
}
