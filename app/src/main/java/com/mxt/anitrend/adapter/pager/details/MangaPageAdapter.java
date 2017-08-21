package com.mxt.anitrend.adapter.pager.details;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.view.detail.fragment.MangaExtrasFragment;
import com.mxt.anitrend.view.detail.fragment.MangaLinksFragment;
import com.mxt.anitrend.view.detail.fragment.MangaOverviewFragment;
import com.mxt.anitrend.view.detail.fragment.MangaReviewFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by Maxwell on 11/3/2016.
 */
public class MangaPageAdapter extends DefaultStatePagerAdapter {

    private int id;

    public MangaPageAdapter(FragmentManager fragmentManager, Context context, int id) {
        super(fragmentManager, context);
        this.id = id;
        mTitles = context.getResources().getStringArray(R.array.manga_page_titles);
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
                return MangaOverviewFragment.newInstance();
            case 1:
                return MangaLinksFragment.newInstance();
            case 2:
                return MangaExtrasFragment.newInstance();
            case 3:
                return MangaReviewFragment.newInstance(id);
        }
        return null;
    }
}