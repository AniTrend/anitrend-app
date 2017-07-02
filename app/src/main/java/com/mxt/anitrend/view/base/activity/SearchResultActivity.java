package com.mxt.anitrend.view.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.SearchPageAdapter;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultActivity extends DefaultActivity implements SearchView.OnQueryTextListener {

    public static final String QUERY = "search_query";

    private String MODEL_SAVE_POSITION = "page_position";
    private final String QUERY_KEY = "key_query";

    private int mPageIndex;
    private String query;

    private SearchPageAdapter mSearchPageAdapter;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.search_pager) ViewPager mViewPager;
    @BindView(R.id.nts_center) SmartTabLayout mNavigationTabStrip;

    private SearchView searchView;
    private ApplicationPrefs mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mPrefs = new ApplicationPrefs(getApplicationContext());
        if(savedInstanceState == null) {
            query = getIntent().getStringExtra(QUERY);
        }
        else {
            query = savedInstanceState.getString(QUERY_KEY);
            mPageIndex = savedInstanceState.getInt(MODEL_SAVE_POSITION);
        }
        updateUI();
    }

    @Override
    protected void updateUI() {
        mSearchPageAdapter = new SearchPageAdapter(getSupportFragmentManager(), query, getApplicationContext());
        mViewPager.setAdapter(mSearchPageAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mNavigationTabStrip.setViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if(searchView != null) {
            searchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
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
        outState.putInt(MODEL_SAVE_POSITION, mPageIndex);
        outState.putString(QUERY_KEY, query);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the
     * keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true
     * to indicate that it has handled the submit request. Otherwise return false to
     * let the SearchView handle the submission by launching any associated intent.
     *
     * @param query the query text that is to be submitted
     *
     * @return true if the query has been handled by the listener, false to let the
     * SearchView perform the default action.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        this.query = query;
        EventBus.getDefault().post(query);
        return false;
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText the new content of the query text field.
     *
     * @return false if the SearchView should perform the default action of showing any
     * suggestions if available, true if the action was handled by the listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
