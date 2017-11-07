package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.ExternalLink;
import com.mxt.anitrend.view.detail.fragment.AnimeWatchFragment;
import com.mxt.anitrend.view.index.fragment.FeedVideoFragment;
import com.mxt.anitrend.view.index.fragment.PlaylistFragment;
import com.mxt.anitrend.view.index.fragment.SuggestionFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/05/02.
 */
public class HubPageAdapter extends DefaultStatePagerAdapter {

    public HubPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        mTitles = context.getResources().getStringArray(R.array.hub_title);
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
                return SuggestionFragment.newInstance();
            case 1:
                List<ExternalLink> externalLinks = new ArrayList<>(1);
                externalLinks.add(new ExternalLink(BuildConfig.FEEDS_LINK,null));
                return AnimeWatchFragment.newInstance(externalLinks, true);
            case 2:
                return PlaylistFragment.newInstance();
        }
        return null;
    }
}