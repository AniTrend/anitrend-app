package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.index.fragment.ReviewFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

/**
 * Created by max on 2017/05/02.
 */

public class ReviewPageAdapter extends DefaultStatePagerAdapter {

    public ReviewPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        mTitles = context.getResources().getStringArray(R.array.reviews_title);
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
                return ReviewFragment.newInstance(KeyUtils.LATEST);
            case 1:
                return ReviewFragment.newInstance(KeyUtils.POPULAR);
            case 2:
                return ReviewFragment.newInstance(KeyUtils.NEED_LOVE);
            case 3:
                return ReviewFragment.newInstance(KeyUtils.CONTROVERSIAL);
        }
        return null;
    }
}