package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mxt.anitrend.base.custom.presenter.CommonPresenter;
import com.mxt.anitrend.base.interfaces.dao.BoxQuery;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.entity.anilist.WebToken;
import com.mxt.anitrend.model.entity.base.AuthBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.AnalyticsUtil;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.ShortcutUtil;

import java.util.concurrent.ExecutionException;

/**
 * Created by max on 2017/10/14.
 * Web token requester
 */

public class WebTokenRequest {

    private static final Object lock = new Object();

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
        presenter.getApplicationPref().setAuthenticated(false);
        presenter.getDatabase().invalidateBoxStores();
        JobSchedulerUtil.cancelJob(context);
        WebFactory.invalidate();
        token = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            ShortcutUtil.removeAllDynamicShortcuts(context);
        AnalyticsUtil.clearSession();
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
                presenter.getDatabase().saveWebToken(response);
                Log.d("WebTokenRequest", "Token refreshed & saved at time stamp: " + System.currentTimeMillis()/1000L);
            }
            else Log.e("WebTokenRequest", "Token had an invalid instance from context: "+context);
        }
    }

    /**
     * Request a new token if the application has not been authenticated,
     * other wise request a new refresh token and replace the current token
     * retaining the refresh token key
     */
    public static void getToken(Context context) {
        synchronized (lock) {
            if(new ApplicationPref(context).isAuthenticated()) {
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
    public static synchronized boolean getToken(Context context, String code) throws ExecutionException, InterruptedException {
        WebToken authenticatedToken = new AuthenticationCodeAsync().execute(code).get();
        if(authenticatedToken != null) {
            createNewTokenReference(authenticatedToken);
            BoxQuery boxQuery = new DatabaseHelper(context);
            boxQuery.saveWebToken(authenticatedToken);
            boxQuery.saveAuthCode(new AuthBase(code, authenticatedToken.getRefresh_token()));
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
            Log.e("createNewTokenReference", e.getMessage());
        }
    }

    private static class AuthenticationCodeAsync extends AsyncTask<String, Void, WebToken> {
        @Override
        protected WebToken doInBackground(String... codes) {
            return WebFactory.requestCodeTokenSync(codes[0]);
        }
    }
}
