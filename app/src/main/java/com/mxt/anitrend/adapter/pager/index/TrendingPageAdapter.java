package com.mxt.anitrend.adapter.pager.index;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mxt.anitrend.R;
import com.mxt.anitrend.view.index.fragment.NewAnimeFragment;
import com.mxt.anitrend.view.index.fragment.TrendingFragment;
import com.mxt.anitrend.viewmodel.pager.DefaultStatePagerAdapter;

import java.util.Locale;

/**
 * Created by Maxwell on 11/1/2016.
 */

public class TrendingPageAdapter extends DefaultStatePagerAdapter {


    public TrendingPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager, context);
        mTitles = context.getResources().getStringArray(R.array.trending_title);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return TrendingFragment.newInstance();
            case 1:
                return NewAnimeFragment.newInstance();
        }
        return null;
    }
}