package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.view.detail.fragment.AnimeWatchFragment;
import com.mxt.anitrend.view.index.fragment.AiringFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by max on 2017/03/04.
 */
public class AiringPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 2;

    public AiringPageAdapter(FragmentManager manager) {
        super(manager);
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
                return AiringFragment.newInstance();
            case 1:
                List<ExternalLink> externalLinks = new ArrayList<>(1);
                externalLinks.add(new ExternalLink(BuildConfig.FEEDS_LINK,null));
                return AnimeWatchFragment.newInstance(externalLinks);
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