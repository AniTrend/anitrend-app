package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.detail.StaffPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.view.widget.FavouriteToolbarWidget;
import com.mxt.anitrend.model.entity.anilist.Staff;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.TapTargetUtil;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by max on 2017/12/14.
 * staff activity
 */

public class StaffActivity extends ActivityBase<Staff, BasePresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;
    protected @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;

    private Staff model;

    private FavouriteToolbarWidget favouriteWidget;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_generic);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setPresenter(new BasePresenter(this));
        setViewModel(true);
        id = getIntent().getLongExtra(KeyUtils.arg_id, 0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getViewModel().getParams().putLong(KeyUtils.arg_id, id);
        StaffPageAdapter pageAdapter = new StaffPageAdapter(getSupportFragmentManager(), getApplicationContext());
        pageAdapter.setParams(getViewModel().getParams());
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(offScreenLimit + 1);
        smartTabLayout.setViewPager(viewPager);
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isAuth = getPresenter().getApplicationPref().isAuthenticated();
        getMenuInflater().inflate(R.menu.custom_menu, menu);
        menu.findItem(R.id.action_favourite).setVisible(isAuth);
        if(isAuth) {
            MenuItem favouriteMenuItem = menu.findItem(R.id.action_favourite);
            favouriteWidget = (FavouriteToolbarWidget) favouriteMenuItem.getActionView();
            if(model != null)
                favouriteWidget.setModel(model);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(model == null)
            makeRequest();
        else
            updateUI();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {

    }

    @Override
    protected void updateUI() {
        getPresenter().notifyAllListeners(model, false);
        if(favouriteWidget != null)
            favouriteWidget.setModel(model);
    }

    @Override
    protected void makeRequest() {
        getViewModel().requestData(KeyUtils.STAFF_INFO_REQ, getApplicationContext());
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable Staff model) {
        super.onChanged(model);
        if(model != null) {
            this.model = model;
            updateUI();
        }
    }
}
