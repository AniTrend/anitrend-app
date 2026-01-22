package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.mxt.anitrend.adapter.pager.detail.MessagePageAdapter;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/12/07.
 * MessageActivity
 */

public class MessageActivity extends ActivityBase<FeedList, BasePresenter> {

    private ActivityPagerGenericBinding binding;

    private MessagePageAdapter messagePageAdapter;

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
        getViewModel().getParams().putLong(KeyUtil.arg_userId, getPresenter().getDatabase().getCurrentUser().getId());
        onActivityReady();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        messagePageAdapter = new MessagePageAdapter(getSupportFragmentManager(), getApplicationContext());
        messagePageAdapter.setParams(getViewModel().getParams());
        updateUI();
    }

    @Override
    protected void updateUI() {
        binding.contentMain.pageContainer.setAdapter(messagePageAdapter);
        binding.contentMain.pageContainer.setOffscreenPageLimit(offScreenLimit);
        binding.customTab.smartTab.setViewPager(binding.contentMain.pageContainer);
    }

    @Override
    protected void makeRequest() {

    }
}
