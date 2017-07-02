package com.mxt.anitrend.adapter.pager.details;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.view.detail.fragment.FavouritesFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/05/16.
 */

public class FavouritesPageAdapter extends DefaultStatePagerAdapter {

    private Favourite model;

    public FavouritesPageAdapter(FragmentManager fragmentManager, Favourite model, Context context) {
        super(fragmentManager, context);
        this.model = model;
        mTitles = context.getResources().getStringArray(R.array.favorites_page_titles);
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
                return FavouritesFragment.newInstance(model.getAnime(), 0);
            case 1:
                return FavouritesFragment.newInstance(model.getCharacter(), 1);
            case 2:
                return FavouritesFragment.newInstance(model.getManga(), 2);
            case 3:
                return FavouritesFragment.newInstance(model.getStaff(), 3);
            case 4:
                return FavouritesFragment.newInstance(model.getStudio(), 4);
        }
        return null;
    }
}
