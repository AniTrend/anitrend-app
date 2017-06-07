package com.mxt.anitrend.adapter.pager.index;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.mxt.anitrend.view.index.fragment.MangaFragment;
import com.mxt.anitrend.view.index.fragment.NewMangaFragment;

import java.util.Locale;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class MangaPageAdapter extends FragmentStatePagerAdapter {

    private static final int pages = 2;

    public MangaPageAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public void startUpdate(ViewGroup container) {
        super.startUpdate(container);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
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

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return pages;
    }
}