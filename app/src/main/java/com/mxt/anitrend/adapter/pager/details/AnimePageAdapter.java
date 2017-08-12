package com.mxt.anitrend.adapter.pager.details;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.view.detail.fragment.AnimeExtrasFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeLinksFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeOverviewFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeReviewsFragment;
import com.mxt.anitrend.view.detail.fragment.AnimeWatchFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by Maxwell on 10/17/2016.
 */

public class AnimePageAdapter extends DefaultStatePagerAdapter {

    private Series model;

    public AnimePageAdapter(FragmentManager fragmentManager, Series model, Context context) {
        super(fragmentManager, context);
        this.model = model;
        mTitles = context.getResources().getStringArray(R.array.anime_page_titles);
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
                return AnimeWatchFragment.newInstance(model.getExternal_links(), false);
            case 3:
                return AnimeExtrasFragment.newInstance(model);
            case 4:
                return AnimeReviewsFragment.newInstance(model.getId());
        }
        return null;
    }
}