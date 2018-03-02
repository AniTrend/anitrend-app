package com.mxt.anitrend.view.activity.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;

public class AboutActivity extends ActivityBase<Void, BasePresenter> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }

    @Override
    protected void makeRequest() {

    }
}
