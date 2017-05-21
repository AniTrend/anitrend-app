package com.mxt.anitrend.view.index.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.presenter.index.LoginPresenter;
import com.mxt.anitrend.utils.DialogManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class LoginActivity extends AppCompatActivity implements LoginPresenter.LoginCallback, View.OnClickListener {

    public static final int LOGIN_RESULT = 56021;
    public static final String KEY_NOTIFICATION_COUNT = "NOTIFICATION_COUNT";
    public static final String KEY_USER_DEFAULT_LOGIN = "USER_DEFAULT_LOGIN";

    private final String KEY_REQUESTING = "pending_computation";
    private final String KEY_STATUS = "current_status";
    private LoginPresenter mPresenter;
    private boolean requesting;
    private String status;
    private Snackbar mSnackbar;

    @BindView(R.id.login_container) CoordinatorLayout mContainer;
    @BindView(R.id.login_progress) ProgressBar mProgressView;
    @BindView(R.id.login_button) AppCompatButton mLoginButton;
    @BindView(R.id.login_status_txt) TextView mStatusText;
    @BindView(R.id.login_help) TextView mHelpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_v2);
        ButterKnife.bind(this);
        mPresenter = new LoginPresenter(this,this);
        mHelpButton.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        status = getString(R.string.text_please_sign_in);
        if(savedInstanceState != null) {
            requesting = savedInstanceState.getBoolean(KEY_REQUESTING);
            status = savedInstanceState.getString(KEY_STATUS);
            mStatusText.setText(status);
            mLoginButton.setEnabled(!requesting);
            mProgressView.setVisibility(requesting?View.VISIBLE:View.GONE);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onBeginChecks();
    }

    private void onBeginChecks(){
        if(mPresenter.getAppPrefs().isAuthenticated()){
            status = getString(R.string.text_already_authenticated);
            mStatusText.setText(status);
            mLoginButton.setVisibility(View.GONE);
        } else {
            mSnackbar = Snackbar.make(mContainer, getString(R.string.create_account), Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.Yes), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent signUp = new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.REGISTER_URL));
                    startActivity(signUp);
                    mSnackbar.dismiss();
                }
            });
            mSnackbar.show();

            if(mPresenter.getAppPrefs().getLoginTip()) {
                new MaterialTapTargetPrompt.Builder(this)
                        //or use ContextCompat.getColor(this, R.color.colorAccent)
                        .setBackgroundColourFromRes(R.color.colorDarkKnight)
                        .setFocalColourFromRes(R.color.colorAccent)
                        .setTarget(mHelpButton)
                        .setPrimaryText("Login Tip")
                        .setSecondaryText("If the application ever signs you out due to E(104) use this.\n" +
                                "\n" +
                                "Tap Here To Dismiss")
                        .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                            @Override
                            public void onHidePrompt(MotionEvent event, boolean tappedTarget) {

                            }

                            @Override
                            public void onHidePromptComplete() {
                                mPresenter.getAppPrefs().setLoginTip();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_REQUESTING, requesting);
        outState.putString(KEY_STATUS, status);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //The intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        Uri uri = intent.getData();
        if(uri != null && uri.toString().startsWith(BuildConfig.REDIRECT_URI)) {
            status = getString(R.string.text_processing);
            mStatusText.setText(status);
            mPresenter.handleLoginCallback(uri);
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the view_model
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
            return;
        }
        if(requesting) {
            mPresenter.candlePendingLogin();
            status = getString(R.string.canceled_by_user);
            requesting = false;
            mStatusText.setText(status);
            mProgressView.setVisibility(View.GONE);
            mLoginButton.setEnabled(!requesting);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onLoginComplete(User user, int notifications) {
        if(user != null && notifications != -1) {
            status = getString(R.string.text_almost_finished);
            mStatusText.setText(status);
            mPresenter.getAppPrefs().saveMiniUserInfo(user);
            Intent intent = new Intent();
            intent.putExtra(KEY_NOTIFICATION_COUNT, notifications);
            intent.putExtra(KEY_USER_DEFAULT_LOGIN, user);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            requesting = false;
            status = getString(R.string.text_operation_unsuccessful);
            mStatusText.setText(status);
            mProgressView.setVisibility(View.GONE);
            mLoginButton.setEnabled(!requesting);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_button:
                requesting = true;
                status = getString(R.string.text_waiting_for_response);
                Intent requestIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceGenerator.API_AUTH_LINK));
                startActivity(requestIntent);
                mStatusText.setText(status);
                mProgressView.setVisibility(View.VISIBLE);
                mLoginButton.setEnabled(!requesting);
                break;
            case R.id.login_help:
                try {
                    new DialogManager(LoginActivity.this).createDialogMessage(getString(R.string.title_anitrend_help), Html.fromHtml(getString(R.string.login_notice)),
                            getString(R.string.Ok), getString(R.string.Help), getString(R.string.Sync_Changes),
                            new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    switch (which) {
                                        case POSITIVE:
                                            dialog.dismiss();
                                            break;
                                        case NEUTRAL:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://anilist.co/settings/list")));
                                            break;
                                        case NEGATIVE:
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://anilist.co/forum/thread/2081")));
                                            break;
                                    }
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}

