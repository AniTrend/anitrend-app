package com.mxt.anitrend.view.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.async.TokenReference;
import com.mxt.anitrend.presenter.index.SplashPresenter;
import com.mxt.anitrend.custom.service.ServiceScheduler;
import com.mxt.anitrend.util.ApiPreferences;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DefaultPreferences;
import com.mxt.anitrend.util.ErrorHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity implements TokenReference.onTokenComplete, Runnable, SplashPresenter.onUserRetrieved, SwipeRefreshLayout.OnRefreshListener {

    private final String KEY_STATUS_MESSAGE = "STATUS_MSG";
    private final String KEY_REQUESTING = "REQ_SENT";
    private final String KEY_SHORTCUT = "SHORTCUT";
    private int shortcut;
    private int status;
    private boolean requesting;
    private int retry_count;

    @BindView(R.id.activity_splash) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.app_status) TextView app_status;
    private ApiPreferences apiPreferences;
    private ApplicationPrefs applicationPrefs;
    private DefaultPreferences defaultPreferences;
    private SplashPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Intent intent = getIntent();

        if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            String data = intent.getDataString();
            switch (data) {
                case "anime":
                    shortcut = R.id.nav_anime;
                    break;
                case "manga":
                    shortcut = R.id.nav_manga;
                    break;
                case "trending":
                    shortcut = R.id.nav_trending;
                    break;
                case "reviews":
                    shortcut = R.id.nav_reviews;
                    break;
            }
        }
        if(status != 0)
            try {
                if(requesting)
                    requesting = false;
                app_status.setText(getString(status));
            } catch(Exception e){
                e.printStackTrace();
            }
        intiApplication();
    }

    private void intiApplication() {
        if(!requesting) {
            app_status.setText(getString(R.string.app_splash_authenticating));
            mPresenter = new SplashPresenter(SplashActivity.this, this);
            new TokenReference(getApplicationContext()).onInitialize(this);
            requesting = true;
        }
    }

    @Override
    public void onTokenSuccess() {
        apiPreferences = new ApiPreferences(getApplicationContext());
        applicationPrefs = new ApplicationPrefs(getApplicationContext());
        defaultPreferences = new DefaultPreferences(getApplicationContext());
        if(!applicationPrefs.isGenresSaved()) {
            status = R.string.app_splash_fetching;
            app_status.post(new Runnable() {
                @Override
                public void run() {
                    app_status.setText(getString(R.string.app_splash_fetching));
                }
            });
            mPresenter.fetchGenres();
        }
        else
            runOnUiThread(this);
    }

    @Override
    public void onTokenFailure(Throwable cause) {
        Log.e("Splash Screen", "Token Response Callback "+cause.getLocalizedMessage() ,cause);
        cause.printStackTrace();
        if(cause.getLocalizedMessage() != null && cause.getLocalizedMessage().length() < 1)
            mPresenter.makeAlerterWarning(getString(R.string.text_unknown_error));
        else
            mPresenter.makeAlerterWarning(cause.getLocalizedMessage());
        authError(false);
    }

    private void authError(boolean nullException){
        if(!nullException) {
            retry_count++;
            requesting = false;
            swipeRefreshLayout.setRefreshing(requesting);
            status = R.string.app_splash_authentication_failed;
            app_status.setText(getString(R.string.app_splash_authentication_failed));
        } else {
            requesting = false;
            swipeRefreshLayout.setRefreshing(requesting);
            status = R.string.app_splash_api_unavailable;
            app_status.setText(getString(R.string.app_splash_api_unavailable));
        }
    }

    private void startApplication(){
        status = R.string.app_splash_loading;
        app_status.setText(getString(R.string.app_splash_loading));

        //if first run
        if (applicationPrefs.checkState()) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.REDIRECT, shortcut);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, StarterActivity.class);
            intent.putExtra(MainActivity.REDIRECT, shortcut);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void run() {
        if(!applicationPrefs.isAuthenticated()) {
            startApplication();
        } else {
            if (defaultPreferences.isNotificationEnabled())
                new ServiceScheduler(getApplicationContext()).scheduleJob();
            else
                new ServiceScheduler(getApplicationContext()).cancelJob();
            app_status.setText(getString(R.string.app_splash_profile));
            mPresenter.execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_REQUESTING, requesting);
        outState.putInt(KEY_STATUS_MESSAGE, status);
        outState.putInt(KEY_SHORTCUT, shortcut);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            requesting = savedInstanceState.getBoolean(KEY_REQUESTING);
            status = savedInstanceState.getInt(KEY_STATUS_MESSAGE);
            shortcut = savedInstanceState.getInt(KEY_SHORTCUT);
        }
    }

    @Override
    public void onFetchComplete(Call<User> user) {
        if(user == null) {
            authError(true);
            return;
        }
        user.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(!isDestroyed() || !isFinishing()) {
                    if (response.isSuccessful() && response.body() != null) {
                        status = R.string.app_splash_loading;
                        app_status.setText(getString(R.string.app_splash_loading));
                        applicationPrefs.saveMiniUserInfo(response.body());
                        apiPreferences.saveUserApiPreferences(response.body());
                        Intent main = new Intent(SplashActivity.this, MainActivity.class);
                        main.putExtra(MainActivity.REDIRECT, shortcut);
                        main.putExtra(MainActivity.USER_PROF, response.body());
                        startActivity(main);
                        finish();
                    } else {
                        authError(false);
                        mPresenter.makeAlerterWarning(ErrorHandler.getError(response).toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if(!isDestroyed()) {
                    requesting = false;
                    swipeRefreshLayout.setRefreshing(requesting);
                    status = R.string.app_splash_authentication_failed;
                    t.printStackTrace();

                    if(!t.getLocalizedMessage().startsWith("java.lang.IllegalStateException: Expected BEGIN_OBJECT")) {
                        app_status.setText(getString(status));
                    }
                    else {
                        app_status.setText(R.string.text_signed_out);
                        applicationPrefs.setUserDeactivated();
                    }
                }
            }
        });
    }

    @Override
    public void onGenreFetchFinish() {
        runOnUiThread(this);
    }

    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        if(!requesting) {
            if(retry_count > 1) {
                retry_count = 0;
                swipeRefreshLayout.setRefreshing(false);
                mPresenter.requestAppReset();
            }
            else {
                swipeRefreshLayout.setRefreshing(true);
                intiApplication();
            }
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}