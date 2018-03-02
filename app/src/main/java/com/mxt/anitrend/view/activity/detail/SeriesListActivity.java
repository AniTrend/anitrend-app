package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.SeriesListPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/14.
 * users anime / manga list impl
 */

public class SeriesListActivity extends ActivityBase<User, BasePresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;
    protected @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_generic);
        setPresenter(new BasePresenter(this));
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setViewModel(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Bundle bundle = getViewModel().getParams();
        bundle.putInt(KeyUtils.arg_series_type, getIntent().getIntExtra(KeyUtils.arg_series_type, KeyUtils.ANIME));
        bundle.putString(KeyUtils.arg_user_name, getIntent().getStringExtra(KeyUtils.arg_user_name));
        SeriesListPageAdapter pageAdapter = new SeriesListPageAdapter(getSupportFragmentManager(), getApplicationContext());
        pageAdapter.setParams(bundle);
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(offScreenLimit + 1);
        smartTabLayout.setViewPager(viewPager);
        setTitle(bundle.getInt(KeyUtils.arg_series_type) == KeyUtils.ANIME? R.string.title_anime_list: R.string.title_manga_list);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_extra).setVisible(false);
        if(mSearchView != null) {
            MenuItem searchItem = menu.findItem(R.id.action_search);
            mSearchView.setMenuItem(searchItem);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        makeRequest();
    }

    @Override
    protected void updateUI() {

    }

    @Override
    protected void makeRequest() {

    }
}
