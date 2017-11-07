package com.mxt.anitrend.view.detail.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.presenter.index.PlaylistPresenter;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.viewmodel.activity.DefaultActivity;


public class PlaylistActivity extends DefaultActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new PlaylistPresenter(getApplicationContext(), KeyUtils.PLAYLIST_COLLECTION);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void updateUI() {

    }
}
