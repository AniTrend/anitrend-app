package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.fragment.search.CharacterSearchFragment;
import com.mxt.anitrend.view.fragment.search.MediaSearchFragment;
import com.mxt.anitrend.view.fragment.search.StaffSearchFragment;
import com.mxt.anitrend.view.fragment.search.StudioSearchFragment;

/**
 * Created by max on 2017/12/20.
 */

public class FavouritePageAdapter extends BaseStatePageAdapter {

    public FavouritePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        setPagerTitles(R.array.favorites_page_titles);
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
                return MediaSearchFragment.newInstance(getParams(), KeyUtils.ANIME);
            case 1:
                return CharacterSearchFragment.newInstance(getParams());
            case 2:
                return MediaSearchFragment.newInstance(getParams(), KeyUtils.MANGA);
            case 3:
                return StaffSearchFragment.newInstance(getParams());
            case 4:
                return StudioSearchFragment.newInstance(getParams());
        }
        return null;
    }
}
