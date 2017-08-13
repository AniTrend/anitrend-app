package com.mxt.anitrend.viewmodel.pager;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/06/19.
 */

public abstract class DefaultStatePagerAdapter extends FragmentStatePagerAdapter {

    protected String[] mTitles;
    protected Context mContext;

    public DefaultStatePagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = (Bundle) super.saveState();
        // Never maintain any states from the base class, just null it out
        if(bundle != null)
            bundle.putParcelableArray("states", null);
        return bundle;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public abstract Fragment getItem(int position);

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mTitles.length;
    }

    /**
     * This method may be called by the ViewPager to obtain a title string
     * to describe the specified page. This method may return null
     * indicating no title for this page. The default implementation returns
     * null.
     *
     * @param position The position of the title requested
     * @return A title for the requested page
     */
    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        return mTitles[position].toUpperCase(locale);
    }
}
