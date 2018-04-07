package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.list.MediaListFragment;

/**
 * Created by max on 2017/12/17.
 * users list page adapter
 */

public class SeriesListPageAdapter extends BaseStatePageAdapter {

    public SeriesListPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.media_list_status);
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        return MediaListFragment.newInstance(getParams(), GraphUtil.getDefaultQuery(true)
                .putVariable(KeyUtil.arg_status, KeyUtil.MediaListStatus[position]));
    }
}
