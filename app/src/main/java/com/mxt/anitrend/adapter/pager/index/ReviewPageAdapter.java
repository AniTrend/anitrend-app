package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.structure.FilterTypes;
import com.mxt.anitrend.view.index.fragment.ReviewFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

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
                return ReviewFragment.newInstance(FilterTypes.ReviewType.LATEST.ordinal());
            case 1:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.POPULAR.ordinal());
            case 2:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.NEED_LOVE.ordinal());
            case 3:
                return ReviewFragment.newInstance(FilterTypes.ReviewType.CONTROVERSIAL.ordinal());
        }
        return null;
    }
}