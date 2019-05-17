package com.mxt.anitrend.view.activity.index;

import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.databinding.ActivityLoginBinding;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.AnalyticsUtil;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.ShortcutUtil;
import com.mxt.anitrend.worker.AuthenticatorWorker;

/**
 * Created by max on 2017/11/03.
 * Authentication activity
 */

public class LoginActivity extends ActivityBase<User, BasePresenter> implements View.OnClickListener {

    private ActivityLoginBinding binding;
    private User model;

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style, also if you wish
     * to apply the default navigation bar style for light themes
     * @see ActivityBase#configureActivity() () if running android Oreo +
     */
    @Override
    protected void configureActivity() {
        setTheme(new ApplicationPref(this).getTheme() == R.style.AppThemeLight ?
                R.style.AppThemeLight_Translucent: R.style.AppThemeDark_Translucent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setPresenter(new BasePresenter(getApplicationContext()));
        setViewModel(true);
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
        binding.setOnClickListener(this);
        if(getPresenter().getApplicationPref().isAuthenticated()) {
            NotifyUtil.makeText(this, R.string.text_already_authenticated, Toast.LENGTH_SHORT).show();
            binding.widgetFlipper.setVisibility(View.GONE);
        } else
            checkNewIntent(getIntent());
    }

    @Override
    protected void updateUI() {
        if(getPresenter().getApplicationPref().isNotificationEnabled())
            JobSchedulerUtil.INSTANCE.scheduleJob(getApplicationContext());
        createApplicationShortcuts();
        finish();
    }

    private void createApplicationShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Bundle SHORTCUT_MY_ANIME_BUNDLE = new Bundle();
            SHORTCUT_MY_ANIME_BUNDLE.putString(KeyUtil.arg_mediaType, KeyUtil.ANIME);
            SHORTCUT_MY_ANIME_BUNDLE.putString(KeyUtil.arg_userName, model.getName());

            Bundle SHORTCUT_MY_MANGA_BUNDLE = new Bundle();
            SHORTCUT_MY_MANGA_BUNDLE.putString(KeyUtil.arg_mediaType, KeyUtil.MANGA);
            SHORTCUT_MY_MANGA_BUNDLE.putString(KeyUtil.arg_userName, model.getName());

            Bundle SHORTCUT_PROFILE_BUNDLE = new Bundle();
            SHORTCUT_PROFILE_BUNDLE.putString(KeyUtil.arg_userName, model.getName());

            ShortcutUtil.createShortcuts(LoginActivity.this,
                    new ShortcutUtil.ShortcutBuilder()
                            .setShortcutType(KeyUtil.SHORTCUT_NOTIFICATION)
                            .build(),
                    new ShortcutUtil.ShortcutBuilder()
                            .setShortcutType(KeyUtil.SHORTCUT_MY_ANIME)
                            .setShortcutParams(SHORTCUT_MY_ANIME_BUNDLE)
                            .build(),
                    new ShortcutUtil.ShortcutBuilder()
                            .setShortcutType(KeyUtil.SHORTCUT_MY_MANGA)
                            .setShortcutParams(SHORTCUT_MY_MANGA_BUNDLE)
                            .build(),
                    new ShortcutUtil.ShortcutBuilder()
                            .setShortcutType(KeyUtil.SHORTCUT_PROFILE)
                            .setShortcutParams(SHORTCUT_PROFILE_BUNDLE)
                            .build());
        }
    }

    @Override
    protected void makeRequest() {

    }

    @Override
    public void onChanged(@Nullable User model) {
        if(isAlive() && (this.model = model) != null) {
            getPresenter().getDatabase().saveCurrentUser(model);
            updateUI();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.auth_sign_in:
                if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WebFactory.API_AUTH_LINK)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getLocalizedMessage());
                        NotifyUtil.makeText(this, R.string.text_unknown_error, Toast.LENGTH_SHORT).show();
                    }
                } else NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
            case R.id.container:
                if(binding.widgetFlipper.getDisplayedChild() != WidgetPresenter.LOADING_STATE)
                    finish();
                else
                    NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void showError(String error) {
        if(isAlive()) {
            WebTokenRequest.invalidateInstance(getApplicationContext());
            if(error == null) error = getString(R.string.text_error_auth_login);
            NotifyUtil.createAlerter(this, getString(R.string.login_error_title),
                    error, R.drawable.ic_warning_white_18dp, R.color.colorStateRed, KeyUtil.DURATION_LONG);
            if (getPresenter() != null && getPresenter().getApplicationPref().isCrashReportsEnabled())
                AnalyticsUtil.reportException(TAG, error);
            binding.widgetFlipper.showPrevious();
            Log.e(this.toString(), error);
        }
    }

    @Override
    public void showEmpty(String message) {
        if(isAlive()) {
            WebTokenRequest.invalidateInstance(getApplicationContext());
            if(message == null) message = getString(R.string.text_error_auth_login);
            NotifyUtil.createAlerter(this, getString(R.string.text_error_request),
                    message, R.drawable.ic_warning_white_18dp, R.color.colorStateOrange, KeyUtil.DURATION_LONG);
            binding.widgetFlipper.showPrevious();
            Log.w(this.toString(), message);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if(!getPresenter().getApplicationPref().isAuthenticated())
            checkNewIntent(intent);
    }

    private void checkNewIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            if (isAlive()) {
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE)
                    binding.widgetFlipper.showNext();

                Data workerInputData = new Data.Builder()
                        .putString(KeyUtil.arg_model, intent.getData().toString())
                        .build();

                OneTimeWorkRequest authenticatorWorker = new OneTimeWorkRequest.Builder(AuthenticatorWorker.class)
                        .addTag(KeyUtil.WorkAuthenticatorTag)
                        .setInputData(workerInputData)
                        .build();
                WorkManager.getInstance().enqueue(authenticatorWorker);
                WorkManager.getInstance().getWorkInfoByIdLiveData(authenticatorWorker.getId())
                        .observe(this, workInfoObserver);
            }
        }
    }

    private final Observer<WorkInfo> workInfoObserver = new Observer<WorkInfo>() {
        @Override
        public void onChanged(@Nullable WorkInfo workInfo) {
            if (workInfo != null && workInfo.getState().isFinished()) {
                Data outputData = workInfo.getOutputData();
                if (outputData.getBoolean(KeyUtil.arg_model, false)) {
                    getViewModel().getParams().putParcelable(KeyUtil.arg_graph_params, GraphUtil.INSTANCE.getDefaultQuery(false));
                    getViewModel().requestData(KeyUtil.USER_CURRENT_REQ, getApplicationContext());
                }
                else {
                    if (!TextUtils.isEmpty(outputData.getString(KeyUtil.arg_uri_error)) && !TextUtils.isEmpty(outputData.getString(KeyUtil.arg_uri_error_description)))
                        NotifyUtil.createAlerter(LoginActivity.this, outputData.getString(KeyUtil.arg_uri_error),
                                outputData.getString(KeyUtil.arg_uri_error_description), R.drawable.ic_warning_white_18dp,
                                R.color.colorStateOrange, KeyUtil.DURATION_LONG);
                    else
                        NotifyUtil.createAlerter(LoginActivity.this, R.string.login_error_title,
                                R.string.text_error_auth_login, R.drawable.ic_warning_white_18dp,
                                R.color.colorStateRed, KeyUtil.DURATION_LONG);
                    binding.widgetFlipper.showPrevious();
                }
            }
        }
    };
}
