package com.mxt.anitrend.view.activity.detail;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.view.activity.index.MainActivity;
import com.mxt.anitrend.view.fragment.detail.NotificationFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/10/25.
 */

public class NotificationActivity extends ActivityBase<Void, BasePresenter> {

    protected @BindView(R.id.toolbar)
    Toolbar toolbar;

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

    @Override
    public void onBackPressed() {
        if (isTaskRoot())
            startActivity(new Intent(this, MainActivity.class));

        super.onBackPressed();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        mFragment = NotificationFragment.Companion.newInstance();
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
