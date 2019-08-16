package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.view.fragment.detail.MediaFeedFragment;
import com.mxt.anitrend.view.fragment.detail.MediaOverviewFragment;
import com.mxt.anitrend.view.fragment.detail.MediaStaffFragment;
import com.mxt.anitrend.view.fragment.detail.MediaStatsFragment;
import com.mxt.anitrend.view.fragment.detail.ReviewFragment;
import com.mxt.anitrend.view.fragment.group.MediaCharacterFragment;
import com.mxt.anitrend.view.fragment.group.MediaRelationFragment;
import com.mxt.anitrend.view.fragment.list.WatchListFragment;

/**
 * Created by max on 2017/12/01.
 */

public class AnimePageAdapter extends BaseStatePageAdapter {

    private boolean isAuthenticated;

    public AnimePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.anime_page_titles);
        isAuthenticated = new Settings(context).isAuthenticated();
    }

    @Override
    public int getCount() {
        return isAuthenticated ? super.getCount() : super.getCount() - 2;
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
                return MediaOverviewFragment.Companion.newInstance(getParams());
            case 1:
                return MediaRelationFragment.newInstance(getParams());
            case 2:
                return MediaStatsFragment.newInstance(getParams());
            case 3:
                return WatchListFragment.newInstance(getParams(),false);
            case 4:
                return MediaCharacterFragment.newInstance(getParams());
            case 5:
                return MediaStaffFragment.newInstance(getParams());
            case 6:
                return MediaFeedFragment.newInstance(getParams(), GraphUtil.INSTANCE.getDefaultQuery(true)
                        .putVariable(KeyUtil.arg_mediaId, getParams().getLong(KeyUtil.arg_id))
                        .putVariable(KeyUtil.arg_type, KeyUtil.ANIME_LIST)
                        .putVariable(KeyUtil.arg_isFollowing, true));
            case 7:
                return ReviewFragment.newInstance(getParams());
        }
        return null;
    }
}
