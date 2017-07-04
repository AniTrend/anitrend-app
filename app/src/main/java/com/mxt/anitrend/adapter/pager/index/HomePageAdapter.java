package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.ProgressFragment;
import com.mxt.anitrend.view.index.fragment.PublicStatusFragment;
import com.mxt.anitrend.view.index.fragment.StatusFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/03/09.
 */

public class HomePageAdapter extends DefaultStatePagerAdapter {

    public HomePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        mTitles = context.getResources().getStringArray(R.array.feed_titles);
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
                return ProgressFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PROGRESS.ordinal()]);
            case 1:
                return StatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.STATUS.ordinal()]);
            case 2:
                return PublicStatusFragment.newInstance(FilterTypes.ActivtyTypes[FilterTypes.ActivityType.PUBIC_STATUS.ordinal()]);
        }
        return null;
    }
}
