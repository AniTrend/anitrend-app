package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.view.fragment.index.SuggestionFragment;
import com.mxt.anitrend.view.fragment.index.WatchFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 2017/11/04.
 */

public class HubPageAdapter extends BaseStatePageAdapter {

    public HubPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.hub_title);
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return super.getCount() - 1;
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
                return WatchFragment.newInstance(externalLinks, true);
            case 2:
                return new Fragment();
        }
        return null;
    }
}
