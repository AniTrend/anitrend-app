package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.detail.MediaFeedFragment;
import com.mxt.anitrend.view.fragment.detail.MediaOverviewFragment;
import com.mxt.anitrend.view.fragment.group.MediaCharacterFragment;
import com.mxt.anitrend.view.fragment.detail.ReviewFragment;
import com.mxt.anitrend.view.fragment.group.MediaRelationFragment;
import com.mxt.anitrend.view.fragment.detail.MediaStatsFragment;
import com.mxt.anitrend.view.fragment.detail.MediaStaffFragment;

/**
 * Created by max on 2017/12/01.
 */

public class MangaPageAdapter extends BaseStatePageAdapter {

    public MangaPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.manga_page_titles);
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
                return MediaOverviewFragment.newInstance(getParams());
            case 1:
                return MediaRelationFragment.newInstance(getParams());
            case 2:
                return MediaStatsFragment.newInstance(getParams());
            case 3:
                return MediaCharacterFragment.newInstance(getParams());
            case 4:
                return MediaStaffFragment.newInstance(getParams());
            case 5:
                return MediaFeedFragment.newInstance(getParams(), GraphUtil.getDefaultQuery(true)
                        .putVariable(KeyUtils.arg_mediaId, getParams().getLong(KeyUtils.arg_id))
                        .putVariable(KeyUtils.arg_type, KeyUtils.MANGA_LIST)
                        .putVariable(KeyUtils.arg_isFollowing, true));
            case 6:
                return ReviewFragment.newInstance(getParams());
        }
        return null;
    }
}