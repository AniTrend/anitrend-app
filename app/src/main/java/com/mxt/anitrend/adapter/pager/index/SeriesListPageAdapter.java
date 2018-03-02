package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.detail.SeriesListFragment;

/**
 * Created by max on 2017/12/17.
 * users list page adapter
 */

public class SeriesListPageAdapter extends BaseStatePageAdapter {

    public SeriesListPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
    }

    private void setSeriesType(@KeyUtils.SeriesType int seriesType) {
        switch (seriesType) {
            case KeyUtils.ANIME:
                setPagerTitles(R.array.anime_listing_status);
                break;
            case KeyUtils.MANGA:
                setPagerTitles(R.array.manga_listing_status);
                break;
        }
    }

    @Override
    public void setParams(Bundle params) {
        super.setParams(params);
        setSeriesType(params.getInt(KeyUtils.arg_series_type));
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        return SeriesListFragment.newInstance(getParams(), position);
    }
}
