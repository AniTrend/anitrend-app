package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.R;
import com.mxt.anitrend.view.index.fragment.MangaFragment;
import com.mxt.anitrend.view.index.fragment.NewMangaFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class MangaPageAdapter extends DefaultStatePagerAdapter {

    public MangaPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        mTitles = context.getResources().getStringArray(R.array.manga_title);
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
                return MangaFragment.newInstance();
            case 1:
                return NewMangaFragment.newInstance();
        }
        return null;
    }
}