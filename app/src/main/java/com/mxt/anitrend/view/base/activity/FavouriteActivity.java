package com.mxt.anitrend.view.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.details.FavouritesPageAdapter;
import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/05/17.
 */

public class FavouriteActivity  extends DefaultActivity {

    public static final String FAVOURITES_PARAM = "favourites_param";

    private String KEY_MODEL = "model_key";

    @BindView(R.id.page_container) ViewPager mViewPager;
    @BindView(R.id.nts_center) SmartTabLayout mNavigationTab;
    @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private Snackbar snackbar;

    private Favourite mFavourite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_browse);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(savedInstanceState == null) {
            Intent intent = getIntent();
            mFavourite = intent.getParcelableExtra(FAVOURITES_PARAM);
        }
        startInit();
    }

    /**
     * Optionally allowed to override
     */
    @Override
    protected void startInit() {
        if(mFavourite != null)
            updateUI();
        else {
            snackbar = Snackbar.make(coordinatorLayout, R.string.text_error_request, Snackbar.LENGTH_INDEFINITE).setAction(R.string.Ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void updateUI() {
        mViewPager.setAdapter(new FavouritesPageAdapter(getSupportFragmentManager(), mFavourite, getApplicationContext()));
        mViewPager.setOffscreenPageLimit(3);
        mNavigationTab.setViewPager(mViewPager);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_MODEL, mFavourite);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mFavourite = savedInstanceState.getParcelable(KEY_MODEL);
        }
    }
}
