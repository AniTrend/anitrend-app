package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by Maxwell on 10/14/2016.
 */
public class SeasonPageAdapter extends BaseStatePageAdapter {

    public SeasonPageAdapter(FragmentManager manager, Context context) {
        super(manager, context);
        setPagerTitles(R.array.seasons_titles);
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
                return SeasonFragment.newInstance(KeyUtil.WINTER);
            case 1:
                return SeasonFragment.newInstance(KeyUtil.SPRING);
            case 2:
                return SeasonFragment.newInstance(KeyUtil.SUMMER);
            case 3:
                return SeasonFragment.newInstance(KeyUtil.FALL);
        }
        return null;
    }
}
