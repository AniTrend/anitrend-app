package com.mxt.anitrend.viewmodel.pager;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by max on 2017/06/19.
 */

public abstract class DefaultStatePagerAdapter extends FragmentStatePagerAdapter {

    protected String[] mTitles;
    protected int mPages;

    public DefaultStatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = (Bundle) super.saveState();
        bundle.putParcelableArray("states", null); // Never maintain any states from the base class, just null it out
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
        return mPages;
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
    public abstract CharSequence getPageTitle(int position);
}
