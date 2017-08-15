package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.index.fragment.ProgressFragment;
import com.mxt.anitrend.view.index.fragment.PublicStatusFragment;
import com.mxt.anitrend.view.index.fragment.StatusFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

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
                return ProgressFragment.newInstance(KeyUtils.ActivityTypes[KeyUtils.PROGRESS]);
            case 1:
                return StatusFragment.newInstance(KeyUtils.ActivityTypes[KeyUtils.STATUS]);
            case 2:
                return PublicStatusFragment.newInstance(KeyUtils.ActivityTypes[KeyUtils.PUBIC_STATUS]);
        }
        return null;
    }
}
