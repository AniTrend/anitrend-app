package com.mxt.anitrend.adapter.pager.detail;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.pager.BaseStatePageAdapter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.view.fragment.favourite.CharacterFavouriteFragment;
import com.mxt.anitrend.view.fragment.favourite.MediaFavouriteFragment;
import com.mxt.anitrend.view.fragment.favourite.StaffFavouriteFragment;
import com.mxt.anitrend.view.fragment.favourite.StudioFavouriteFragment;

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
                return MediaFavouriteFragment.newInstance(getParams(), KeyUtil.ANIME);
            case 1:
                return CharacterFavouriteFragment.newInstance(getParams());
            case 2:
                return MediaFavouriteFragment.newInstance(getParams(), KeyUtil.MANGA);
            case 3:
                return StaffFavouriteFragment.newInstance(getParams());
            case 4:
                return StudioFavouriteFragment.newInstance(getParams());
        }
        return null;
    }
}
