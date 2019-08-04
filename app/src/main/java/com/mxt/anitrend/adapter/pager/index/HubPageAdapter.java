package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.model.entity.anilist.ExternalLink;
import com.mxt.anitrend.view.fragment.list.SuggestionListFragment;
import com.mxt.anitrend.view.fragment.list.WatchListFragment;

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
                return SuggestionListFragment.newInstance(getParams());
            case 1:
                List<ExternalLink> externalLinks = new ArrayList<>(1);
                externalLinks.add(new ExternalLink(BuildConfig.FEEDS_LINK,null));
                return WatchListFragment.newInstance(externalLinks, true);
        }
        return null;
    }
}
