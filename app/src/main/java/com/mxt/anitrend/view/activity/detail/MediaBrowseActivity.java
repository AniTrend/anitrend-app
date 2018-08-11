package com.mxt.anitrend.view.activity.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.presenter.fragment.MediaPresenter;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MarkDownUtil;
import com.mxt.anitrend.view.fragment.list.MediaBrowseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2018/01/27.
 * browse activity for rankings, tags and genres.
 */

public class MediaBrowseActivity extends ActivityBase<MediaBase, MediaPresenter> {

    protected @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_generic);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setViewModel(true);
        setPresenter(new BasePresenter(this));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(getIntent().hasExtra(KeyUtil.arg_activity_tag)) {
            Spanned activityTitle = MarkDownUtil.convert(getIntent().getStringExtra(KeyUtil.arg_activity_tag));
            mActionBar.setTitle(activityTitle);
        }
        onActivityReady();
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
        mFragment = MediaBrowseFragment.newInstance(getIntent().getExtras());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, mFragment, mFragment.TAG);
        fragmentTransaction.commit();
    }

    @Override
    protected void makeRequest() {
        updateUI();
    }
}
