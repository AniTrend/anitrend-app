package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/10/30.
 */

public class TrendingPageAdapter extends BaseStatePageAdapter {

    public TrendingPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.trending_title);
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
                return AnimeFragment.newInstance(KeyUtil.BROWSE_ANIME_TRENDING_REQ);
            case 1:
                return AnimeFragment.newInstance(KeyUtil.BROWSE_ANIME_LATEST_REQ);
        }
        return null;
    }
}
