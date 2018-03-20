package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.model.entity.anilist.FeedList;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.view.fragment.detail.CommentFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/11/15.
 * Comment activity for progress & feeds
 */

public class CommentActivity extends ActivityBase<FeedList, BasePresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_generic);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setPresenter(new BasePresenter(this));
        onActivityReady();
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(mFragment != null)
            if(mFragment.onBackPress())
                return;
        super.onBackPressed();
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
