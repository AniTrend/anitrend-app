package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.view.index.fragment.CompletedAnimeFragment;
import com.mxt.anitrend.view.index.fragment.DroppedAnimeFragment;
import com.mxt.anitrend.view.index.fragment.OnHoldAnimeFragment;
import com.mxt.anitrend.view.index.fragment.PlanToWatchFragment;
import com.mxt.anitrend.view.index.fragment.WatchingFragment;

import java.util.Locale;

/**
 * Created by Maxwell on 11/20/2016.
 */

public class MyAnimePageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 5;
    private int user_id;
    private final String[] mTitles;

    public MyAnimePageAdapter(FragmentManager manager, int id, String[] titles) {
        super(manager);
        user_id = id;
        mTitles = titles;
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
                return WatchingFragment.newInstance(user_id);
            case 1:
                return PlanToWatchFragment.newInstance(user_id);
            case 2:
                return CompletedAnimeFragment.newInstance(user_id);
            case 3:
                return OnHoldAnimeFragment.newInstance(user_id);
            case 4:
                return DroppedAnimeFragment.newInstance(user_id);
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


    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        return mTitles[position].toUpperCase(locale);
    }
}
