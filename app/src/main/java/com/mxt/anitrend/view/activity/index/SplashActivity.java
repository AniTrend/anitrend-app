package com.mxt.anitrend.view.activity.index;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.view.image.WideImageView;
import com.mxt.anitrend.model.entity.base.VersionBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.AnalyticsUtil;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.DateUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.view.activity.base.WelcomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/10/04.
 * Base splash screen
 */

public class SplashActivity extends ActivityBase<VersionBase, BasePresenter> {

    protected @BindView(R.id.preview_credits)
    WideImageView giphyCitation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        setPresenter(new BasePresenter(this));
        setViewModel(true);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        giphyCitation.setImageResource(!CompatUtil.isLightTheme(this) ? R.drawable.powered_by_giphy_light : R.drawable.powered_by_giphy_dark);
        onActivityReady();
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    @Override
    protected void onActivityReady() {
        getPresenter().checkGenresAndTags(this);
        makeRequest();
    }

    @Override
    protected void updateUI() {
        if(isAlive()) {
            boolean freshInstall = getPresenter().getApplicationPref().isFreshInstall();
            Intent intent = new Intent(SplashActivity.this, freshInstall?WelcomeActivity.class:MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void makeRequest() {
        VersionBase versionBase = getPresenter().getDatabase().getRemoteVersion();
        if(versionBase == null || DateUtil.timeDifferenceSatisfied(KeyUtils.TIME_UNIT_HOURS, versionBase.getLast_checked(), 6))
            getViewModel().requestData(KeyUtils.UPDATE_CHECKER_REQ, getApplicationContext());
        else
            updateUI();
    }

    /**
     * Called when the model state is changed.
     *
     * @param model The new data
     */
    @Override
    public void onChanged(@Nullable VersionBase model) {
        super.onChanged(model);
        if(model != null) {
            getPresenter().getDatabase().saveRemoteVersion(model);
            AnalyticsUtil.logEvent(getPresenter().getApplicationPref(),
                    getApplicationContext(), "application_version",
                    model.getVersion());
        }
        updateUI();
    }

    @Override
    public void showError(String error) {
        updateUI();
    }

    @Override
    public void showEmpty(String message) {
        updateUI();
    }
}
