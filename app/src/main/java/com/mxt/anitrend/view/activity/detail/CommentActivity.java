package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.view.fragment.detail.CommentFragment;

/**
 * Created by max on 2017/11/15.
 * Comment activity for progress & feeds
 */

public class CommentActivity extends ActivityBase<FeedList, BasePresenter> {

    private ActivityFrameGenericBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFrameGenericBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mSearchView = binding.customToolbar.searchView;
        setSupportActionBar(binding.customToolbar.toolbar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setPresenter(new BasePresenter(this));
        onActivityReady();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        mFragment = CommentFragment.newInstance(getIntent().getExtras());
        updateUI();
    }

    @Override
    protected void updateUI() {
        if(mFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, mFragment, mFragment.TAG);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void makeRequest() {

    }
}
