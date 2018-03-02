package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.detail.FavouritePageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/14.
 */

public class FavouriteActivity extends ActivityBase<Favourite, BasePresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;
    protected @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;

    private Favourite model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_generic);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setPresenter(new BasePresenter(this));
        setViewModel(true);
        id = getIntent().getLongExtra(KeyUtils.arg_user_id, 0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        FavouritePageAdapter pageAdapter = new FavouritePageAdapter(getSupportFragmentManager(), getApplicationContext());
        getViewModel().getParams().putLong(KeyUtils.arg_user_id, id);
        pageAdapter.setParams(getViewModel().getParams());
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(offScreenLimit + 1);
        smartTabLayout.setViewPager(viewPager);
        onActivityReady();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        if(model == null)
            makeRequest();
        else
            updateUI();
    }

    @Override
    protected void updateUI() {
        getPresenter().notifyAllListeners(model, false);
    }

    @Override
    protected void makeRequest() {
        getViewModel().requestData(KeyUtils.USER_FAVOURITES_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable Favourite model) {
        super.onChanged(model);
        if(model != null) {
            this.model = model;
            updateUI();
        }
    }
}
