package com.mxt.anitrend.view.activity.index;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.mxt.anitrend.adapter.pager.index.SearchPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding;
import com.mxt.anitrend.presenter.base.BasePresenter;

public class SearchActivity extends ActivityBase<Void, BasePresenter> {

    private ActivityPagerGenericBinding binding;

    private SearchPageAdapter pageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPagerGenericBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mSearchView = binding.customToolbar.searchView;
        setSupportActionBar(binding.customToolbar.toolbar);
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
        binding.contentMain.pageContainer.setAdapter(pageAdapter);
        binding.contentMain.pageContainer.setOffscreenPageLimit(offScreenLimit + 2);
        binding.customTab.smartTab.setViewPager(binding.contentMain.pageContainer);
    }

    @Override
    protected void makeRequest() {

    }
}
