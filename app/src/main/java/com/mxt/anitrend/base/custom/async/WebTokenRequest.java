package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;

import com.mxt.anitrend.analytics.contract.ISupportAnalytics;
import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.base.interfaces.dao.BoxQuery;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.extension.KoinExt;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.Settings;
import com.mxt.anitrend.util.ShortcutUtil;

import java.util.concurrent.ExecutionException;

import timber.log.Timber;

/**
 * Created by max on 2017/10/14.
 * Web token requester
 */

public class WebTokenRequest {

    private static final Object lock = new Object();

    private final static String TAG = "WebTokenRequest";
    private static volatile WebToken token;

    public static WebToken getInstance() {
        return token;
    }

    /**
     * Invalidate authentication state, defaulting to signed out state
     * and disable sync services
     */
    public static void invalidateInstance(Context context) {
        CommonPresenter presenter = new BasePresenter(context);
        presenter.getSettings().setAuthenticated(false);
        presenter.getSettings().setLastDismissedNotificationId(-1);
        presenter.getDatabase().invalidateBoxStores();
        KoinExt.get(JobSchedulerUtil.class).cancelNotificationJob(context);
        WebFactory.invalidate();
        token = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            ShortcutUtil.removeAllDynamicShortcuts(context);
        KoinExt.get(ISupportAnalytics.class).clearUserSession();
    }

    /**
     * Double checks to assure that multiple threads attempting to access
     * the token don't invoke multiple refresh token requests all at once
     */
    private static void checkTokenState(Context context, BasePresenter presenter) {
        if(token == null || token.getExpires() < (System.currentTimeMillis() / 1000L)) {
            WebToken response = WebFactory.requestCodeTokenSync(presenter.getDatabase().getAuthCode().getCode());
            if(response != null) {
                createNewTokenReference(response);
                presenter.getDatabase().setWebToken(response);
                Timber.tag(TAG).d("Token refreshed & saved at time stamp: %s", System.currentTimeMillis() / 1000L);
            }
            else
                Timber.tag(TAG).e("Token had an invalid instance from context: %s", context);
        }
    }

    /**
     * Request a new token if the application has not been authenticated,
     * other wise request a new refresh token and replace the current token
     * retaining the refresh token key
     */
    public static void getToken(Context context) {
        synchronized (lock) {
            if(KoinExt.get(Settings.class).isAuthenticated()) {
                BasePresenter presenter = new BasePresenter(context);
                if (token == null || token.getExpires() < (System.currentTimeMillis() / 1000L)) {
                    token = presenter.getDatabase().getWebToken();
                    checkTokenState(context, presenter);
                }
            }
        }
    }

    /**
     * Request a new access token using access code for authenticated content,
     * and replace the current token with the new one from the server after authentication
     */
    public static synchronized boolean getToken(String code) throws ExecutionException, InterruptedException {
        WebToken authenticatedToken = new AuthenticationCodeAsync().execute(code).get();
        if(authenticatedToken != null) {
            createNewTokenReference(authenticatedToken);
            BoxQuery boxQuery = new DatabaseHelper();
            boxQuery.setWebToken(authenticatedToken);
            boxQuery.setAuthCode(new AuthBase(code, authenticatedToken.getRefresh_token()));
            return true;
        }
        return false;
    }

    /**
     * Copies valid token data from a newly received token into the
     * existing token instance for persistence
     */
    private static void createNewTokenReference(@NonNull WebToken webToken) {
        try {
            webToken.calculateExpires();
            token = webToken.clone();
        } catch (CloneNotSupportedException e) {
            Timber.tag(TAG).e(e,"createNewTokenReference failed");
        }
    }

    private static class AuthenticationCodeAsync extends AsyncTask<String, Void, WebToken> {
        @Override
        protected WebToken doInBackground(String... codes) {
            return WebFactory.requestCodeTokenSync(codes[0]);
        }
    }
}
