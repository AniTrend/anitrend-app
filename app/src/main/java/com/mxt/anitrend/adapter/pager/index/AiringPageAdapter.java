package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.view.fragment.list.AiringListFragment;
import com.mxt.anitrend.view.fragment.list.WatchListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/11/03.
 */

public class AiringPageAdapter extends BaseStatePageAdapter {

    public AiringPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.airing_title);
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
                return AiringListFragment.newInstance();
            case 1:
                List<ExternalLink> externalLinks = new ArrayList<>(1);
                externalLinks.add(new ExternalLink(BuildConfig.FEEDS_LINK,null));
                return WatchListFragment.newInstance(externalLinks, false);
        }
        return null;
    }
}
