package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.pager.index.MediaListPageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/12/14.
 * users anime / manga list impl
 */

public class MediaListActivity extends ActivityBase<User, BasePresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;
    protected @BindView(R.id.page_container) ViewPager viewPager;
    protected @BindView(R.id.smart_tab) SmartTabLayout smartTabLayout;
    protected @BindView(R.id.coordinator) CoordinatorLayout coordinatorLayout;

    private MediaListPageAdapter pageAdapter;

    private @KeyUtil.MediaType String mediaType;

    private Bundle bundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_generic);
        setPresenter(new BasePresenter(this));
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setViewModel(true);
        if((bundle = getIntent().getExtras()) != null)
            mediaType = bundle.getString(KeyUtil.arg_mediaType);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(bundle != null)
            setTitle(CompatUtil.INSTANCE.equals(mediaType, KeyUtil.ANIME) ? R.string.title_anime_list: R.string.title_manga_list);
        onActivityReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_extra).setVisible(false);
        menu.findItem(R.id.action_share).setVisible(false);
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
        pageAdapter = new MediaListPageAdapter(getSupportFragmentManager(), getApplicationContext());
        pageAdapter.setParams(bundle);
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
