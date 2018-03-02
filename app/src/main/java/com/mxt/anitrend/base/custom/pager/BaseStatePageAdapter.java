package com.mxt.anitrend.base.custom.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Locale;

/**
 * Created by max on 2017/06/26.
 * Base page state adapter
 */

public abstract class BaseStatePageAdapter extends FragmentStatePagerAdapter {

    private Bundle params;
    private Context context;
    private String[] mTitles;

    public BaseStatePageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    public void setPagerTitles(@ArrayRes int mTitleRes) {
        mTitles = context.getResources().getStringArray(mTitleRes);
    }

    public String[] getPagerTitles() {
        return mTitles;
    }

    public Bundle getParams() {
        if(params == null)
            params = new Bundle();
        return params;
    }

    public void setParams(Bundle params) {
        this.params = params;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mTitles.length;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public abstract Fragment getItem(int position);

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
