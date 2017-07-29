package com.mxt.anitrend.viewmodel.activity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Toolbar;

import com.mxt.anitrend.R;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.PatternMatcher;

/**
 * Created by max on 2017/03/26.
 * AppCompatActivity extender to make activity changes on start
 */
public abstract class DefaultActivity<T> extends AppCompatActivity {

    protected String mIntentData;
    protected ActionBar mActionBar;
    protected CommonPresenter<T> mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(new ApplicationPrefs(this).isLightTheme() ? R.style.DarkTheme : R.style.DarkTheme_DarkSide);
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if(data != null)
            mIntentData = PatternMatcher.findIntentKey(data.getPath());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Set a {@link Toolbar Toolbar} to act as the
     * {@link ActionBar} for this Activity window.
     * <p>
     * <p>When set to a non-null value the {@link #getActionBar()} method will return
     * an {@link ActionBar} object that can be used to control the given
     * toolbar as if it were a traditional window decor action bar. The toolbar's menu will be
     * populated with the Activity's options menu and the navigation button will be wired through
     * the standard {@link android.R.id#home home} menu select action.</p>
     * <p>
     * <p>In order to use a Toolbar within the Activity's window content the application
     * must not request the window feature
     * {@link Window#FEATURE_ACTION_BAR FEATURE_SUPPORT_ACTION_BAR}.</p>
     *
     * @param toolbar Toolbar to set as the Activity's action bar, or {@code null} to clear it
     */
    @Override
    public void setSupportActionBar(@Nullable android.support.v7.widget.Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        setHomeUp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPresenter != null)
            mPresenter.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(mPresenter != null)
            mPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        if(mPresenter != null)
            mPresenter.onDestroy();
        super.onDestroy();
    }

    private void setHomeUp() {
        if((mActionBar = getSupportActionBar()) != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            int color = ContextCompat.getColor(this, android.R.color.transparent);
            window.setStatusBarColor(color);
        }
    }

    protected boolean isAlive() {
        return !isFinishing() || !isDestroyed();
    }

    /**
     * Optionally allowed to override
     */
    protected void startInit() {

    }

    protected abstract void updateUI();
}
