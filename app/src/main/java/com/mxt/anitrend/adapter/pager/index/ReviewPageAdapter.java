package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.index.FragmentFeedReview;

/**
 * Created by max on 2017/10/30.
 */

public class ReviewPageAdapter extends BaseStatePageAdapter {

    public ReviewPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.reviews_title);
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
                return FragmentFeedReview.newInstance(KeyUtils.LATEST);
            case 1:
                return FragmentFeedReview.newInstance(KeyUtils.POPULAR);
            case 2:
                return FragmentFeedReview.newInstance(KeyUtils.NEED_LOVE);
            case 3:
                return FragmentFeedReview.newInstance(KeyUtils.CONTROVERSIAL);
        }
        return null;
    }
}
