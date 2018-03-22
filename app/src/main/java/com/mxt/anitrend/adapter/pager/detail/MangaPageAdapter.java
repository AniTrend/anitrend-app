package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.group.CharacterFragment;
import com.mxt.anitrend.view.fragment.detail.ReviewFragment;
import com.mxt.anitrend.view.fragment.detail.SeriesOverviewFragment;
import com.mxt.anitrend.view.fragment.group.SeriesRelationFragment;
import com.mxt.anitrend.view.fragment.detail.SeriesStatsFragment;
import com.mxt.anitrend.view.fragment.detail.SocialFragment;
import com.mxt.anitrend.view.fragment.detail.StaffFragment;

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
        getParams().putInt(KeyUtils.arg_media_type, KeyUtils.MANGA);
        getParams().putInt(KeyUtils.arg_request_type, KeyUtils.LIST);
        switch (position) {
            case 0:
                return SeriesOverviewFragment.newInstance(getParams());
            case 1:
                return SeriesRelationFragment.newInstance(getParams());
            case 2:
                return SeriesStatsFragment.newInstance(getParams());
            case 3:
                return CharacterFragment.newInstance(getParams());
            case 4:
                return StaffFragment.newInstance(getParams());
            case 5:
                return SocialFragment.newInstance(getParams());
            case 6:
                return ReviewFragment.newInstance(getParams());
        }
        return null;
    }
}