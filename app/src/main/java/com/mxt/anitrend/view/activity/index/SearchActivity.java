package com.mxt.anitrend.view.activity.index;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.SearchPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends ActivityBase<Void, BasePresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;
    protected @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;

    private SearchPageAdapter pageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_generic);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setPresenter(new BasePresenter(this));
        setViewModel(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onActivityReady();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        pageAdapter = new SearchPageAdapter(getSupportFragmentManager(), getApplicationContext());
        pageAdapter.setParams(getIntent().getExtras());
        updateUI();
    }

    @Override
    protected void updateUI() {
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(offScreenLimit);
        smartTabLayout.setViewPager(viewPager);
    }

    @Override
    protected void makeRequest() {

    }
}
