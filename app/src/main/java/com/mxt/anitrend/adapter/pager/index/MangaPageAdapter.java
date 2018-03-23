package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtils;

/**
 * Created by max on 2017/10/30.
 * Discover Manga Page Adapter
 */

public class MangaPageAdapter extends BaseStatePageAdapter {

    public MangaPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.manga_title);
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
                return MangaFragment.newInstance(KeyUtils.BROWSE_MANGA_REQ);
            case 1:
                return MangaFragment.newInstance(KeyUtils.BROWSE_MANGA_LATEST_REQ);
        }
        return null;
    }
}
