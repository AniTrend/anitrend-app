package com.mxt.anitrend.view.activity.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.view.activity.index.MainActivity;
import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

/**
 * Created by max on 2017/11/09.
 */

public class WelcomeActivity extends ActivityBase<Void, BasePresenter> implements PaperOnboardingOnRightOutListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setTransparentStatusBar();
        setViewModel(true);
        setPresenter(new BasePresenter(this));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
        PaperOnboardingFragment mFragment = PaperOnboardingFragment.newInstance(getIntroductionPages());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, mFragment, mFragment.getTag());
        fragmentTransaction.commit();
        mFragment.setOnRightOutListener(this);
    }

    @Override
    protected void makeRequest() {
        updateUI();
    }

    private ArrayList<PaperOnboardingPage> getIntroductionPages() {
        return new ArrayList<>(CompatUtil.constructListFrom(
                new PaperOnboardingPage(getString(R.string.app_greeting), getString(R.string.app_provider),
                        Color.parseColor("#c26fc1ea"), R.mipmap.ic_launcher, R.drawable.ic_fiber_new_white_24dp),
                new PaperOnboardingPage(getString(R.string.app_intro_colors_title), getString(R.string.app_intro_colors_text),
                        Color.parseColor("#c248c76d"), R.drawable.ic_format_paint_white_24dp, R.drawable.ic_format_paint_white_24dp),
                new PaperOnboardingPage(getString(R.string.app_intro_content_title), getString(R.string.app_intro_content_text),
                        Color.parseColor("#c2f7464a"), R.drawable.ic_bubble_chart_white_24dp, R.drawable.ic_bubble_chart_white_24dp),
                new PaperOnboardingPage(getString(R.string.app_intro_search_title), getString(R.string.app_intro_search_text),
                        Color.parseColor("#c29256f3"), R.drawable.ic_search_white_24dp, R.drawable.ic_search_white_24dp),
                new PaperOnboardingPage(getString(R.string.app_intro_videos_title), getString(R.string.app_intro_videos_text),
                        Color.parseColor("#c2fba640"), R.drawable.ic_slow_motion_video_white_24dp, R.drawable.ic_slow_motion_video_white_24dp)
        ));
    }

    @Override
    public void onRightOut() {
        CompatUtil.startRevealAnim(this, getWindow().getDecorView(), new Intent(WelcomeActivity.this, MainActivity.class), true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            if (hasFocus)
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
