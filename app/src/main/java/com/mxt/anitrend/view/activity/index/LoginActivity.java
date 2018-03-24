package com.mxt.anitrend.view.activity.index;

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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.activity.ActivityBase;
import com.mxt.anitrend.base.custom.async.WebTokenRequest;
import com.mxt.anitrend.databinding.ActivityLoginBinding;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.MessageBase;
import com.mxt.anitrend.presenter.activity.LoginPresenter;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.service.AuthenticatorService;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotifyUtil;
import com.mxt.anitrend.util.ShortcutHelper;

/**
 * Created by max on 2017/11/03.
 * Authentication activity
 */

public class LoginActivity extends ActivityBase<User, LoginPresenter> implements View.OnClickListener {

    private ActivityLoginBinding binding;
    private User model;

    /**
     * Some activities may have custom themes and if that's the case
     * override this method and set your own theme style, also if you wish
     * to apply the default navigation bar style for light themes
     * @see ActivityBase#setNavigationStyle() if running android Oreo +
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
        setPresenter(new LoginPresenter(getApplicationContext()));
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
            JobSchedulerUtil.scheduleJob(getApplicationContext());
        startService(new Intent(LoginActivity.this, AuthenticatorService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Bundle SHORTCUT_MY_ANIME_BUNDLE = new Bundle();
            SHORTCUT_MY_ANIME_BUNDLE.putInt(KeyUtils.arg_mediaType, KeyUtils.ANIME);
            SHORTCUT_MY_ANIME_BUNDLE.putString(KeyUtils.arg_userName, model.getName());

            Bundle SHORTCUT_MY_MANGA_BUNDLE = new Bundle();
            SHORTCUT_MY_MANGA_BUNDLE.putInt(KeyUtils.arg_mediaType, KeyUtils.MANGA);
            SHORTCUT_MY_MANGA_BUNDLE.putString(KeyUtils.arg_userName, model.getName());

            Bundle SHORTCUT_PROFILE_BUNDLE = new Bundle();
            SHORTCUT_PROFILE_BUNDLE.putString(KeyUtils.arg_userName, model.getName());

            ShortcutHelper.createShortcuts(LoginActivity.this,
                    new ShortcutHelper.ShortcutBuilder()
                            .setShortcutType(KeyUtils.SHORTCUT_NOTIFICATION)
                            .build(),
                    new ShortcutHelper.ShortcutBuilder()
                            .setShortcutType(KeyUtils.SHORTCUT_MY_ANIME)
                            .setShortcutParams(SHORTCUT_MY_ANIME_BUNDLE)
                            .build(),
                    new ShortcutHelper.ShortcutBuilder()
                            .setShortcutType(KeyUtils.SHORTCUT_MY_MANGA)
                            .setShortcutParams(SHORTCUT_MY_MANGA_BUNDLE)
                            .build(),
                    new ShortcutHelper.ShortcutBuilder()
                            .setShortcutType(KeyUtils.SHORTCUT_PROFILE)
                            .setShortcutParams(SHORTCUT_PROFILE_BUNDLE)
                            .build());
        }
        finish();
    }

    @Override
    protected void makeRequest() {

    }

    @Override
    public void onChanged(@Nullable User model) {
        if(isAlive() && model != null) {
            getPresenter().getDatabase().saveCurrentUser(model);
            this.model = model;
            updateUI();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.auth_sign_in:
                if(binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE) {
                    binding.widgetFlipper.showNext();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WebFactory.API_AUTH_LINK)));
                } else NotifyUtil.makeText(this, R.string.busy_please_wait, Toast.LENGTH_SHORT).show();
                break;
            case R.id.container:
                finish();
                break;
        }
    }

    @Override
    public void showError(String error) {
        if(isAlive()) {
            WebTokenRequest.invalidateInstance(getApplicationContext());
            if(error == null) error = getString(R.string.text_error_auth_login);
            NotifyUtil.createAlerter(this, getString(R.string.login_error_title),
                    error, R.drawable.ic_warning_white_18dp, R.color.colorStateRed, KeyUtils.DURATION_LONG);
            viewModel.getParams().putString(KeyUtils.key_analytics_error, error);
            FirebaseAnalytics.getInstance(this).logEvent(this.toString(), viewModel.getParams());
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
                    message, R.drawable.ic_warning_white_18dp, R.color.colorStateOrange, KeyUtils.DURATION_LONG);
            binding.widgetFlipper.showPrevious();
            Log.w(this.toString(), message);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(!getPresenter().getApplicationPref().isAuthenticated())
            checkNewIntent(intent);
    }

    private void checkNewIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            MessageBase messageBase = new MessageBase(intent.getData());
            if (isAlive() && messageBase.isValid()) {
                if (binding.widgetFlipper.getDisplayedChild() == WidgetPresenter.CONTENT_STATE)
                    binding.widgetFlipper.showNext();
                if (getPresenter().handleIntentCallback(messageBase))
                    viewModel.requestData(KeyUtils.CURRENT_USER_REQ, getApplicationContext());
                else {
                    // intent://com.mxt.anitrend?error=access_denied&error_description=The+resource+owner+or+authorization+server+denied+the+request.
                    if (!TextUtils.isEmpty(messageBase.getQueryParam("error")) && !TextUtils.isEmpty(messageBase.getQueryParam("error_description")))
                        NotifyUtil.createAlerter(this, messageBase.getQueryParam("error"),
                                messageBase.getQueryParam("error_description"), R.drawable.ic_warning_white_18dp,
                                R.color.colorStateOrange, KeyUtils.DURATION_LONG);
                    else
                        NotifyUtil.createAlerter(this, R.string.login_error_title,
                                R.string.text_error_auth_login, R.drawable.ic_warning_white_18dp,
                                R.color.colorStateRed, KeyUtils.DURATION_LONG);
                    binding.widgetFlipper.showPrevious();
                }
            }
        }
    }
}
