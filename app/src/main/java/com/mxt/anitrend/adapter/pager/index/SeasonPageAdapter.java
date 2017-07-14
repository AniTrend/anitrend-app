package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.index.fragment.SeasonFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

/**
 * Created by Maxwell on 10/14/2016.
 */
public class SeasonPageAdapter extends DefaultStatePagerAdapter {

    public SeasonPageAdapter(FragmentManager manager, Context context) {
        super(manager, context);
        mTitles = context.getResources().getStringArray(R.array.seasons_titles);
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
                return SeasonFragment.newInstance(KeyUtils.WINTER);
            case 1:
                return SeasonFragment.newInstance(KeyUtils.SPRING);
            case 2:
                return SeasonFragment.newInstance(KeyUtils.SUMMER);
            case 3:
                return SeasonFragment.newInstance(KeyUtils.FALL);
        }
        return null;
    }
}
