package com.mxt.anitrend.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mxt.anitrend.api.core.Cache;
import com.mxt.anitrend.api.core.Token;
import com.mxt.anitrend.api.service.AuthService;
import com.mxt.anitrend.custom.RequestRunner;
import com.mxt.anitrend.util.ApplicationPrefs;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 10/9/2016.
 */
public class TokenReference implements Callback<Token> {

    private Context mContext;
    private Cache cache;
    private static Token tokenInstance;
    private onTokenComplete completeCallback;
    private ApplicationPrefs prefs;

    public static Token getInstance() {
        return tokenInstance;
    }

    public static void invalidateInstance() {
        tokenInstance = null;
    }

    public TokenReference(Context context) {
        prefs = new ApplicationPrefs(context);
        mContext = context;
    }

    /**
     * Only used by our index view_model, splash screen
     */
    public void onInitialize(onTokenComplete listener) {
        completeCallback = listener;
        new AsyncRunner(this).execute();
    }

    public Token reInitInstance() {
        cache = new Cache(mContext);

        try {
            tokenInstance = cache.getCachedToken();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(tokenInstance == null || tokenInstance.getExpires() < (System.currentTimeMillis() / 1000L))
            if(prefs.isAuthenticated()) {
                if (((tokenInstance = AuthService.requestRefreshTokenSync(prefs.getRefresh())) != null))
                    try {
                        cache.saveTokenToCache(tokenInstance);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                else {
                    Log.e("requestRefreshTokenSync", "received tokenInstance from requestRefreshTokenSync(String refresh) was null");
                }
            }
            else {
                if ((tokenInstance = AuthService.requestClientTokenSync()) != null)
                    try {
                        cache.saveTokenToCache(tokenInstance);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                else
                    Log.e("requestClientTokenSync", "received tokenInstance from requestClientTokenSync() was null");
            }
        return tokenInstance;
    }

    public boolean AuthorizeNewToken(String code) {
        Boolean result = false;
        try {
            result = new TokenUpdaterAsync().execute(code).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call
     * @param response
     */
    @Override
    public void onResponse(Call<Token> call, Response<Token> response) {
        if(response.isSuccessful() && response.body() != null) {
            try { // assure that we don't cache a null token
                cache.saveTokenToCache(response.body());
                tokenInstance = response.body();
                completeCallback.onTokenSuccess();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(response.errorBody() != null)
        {
            try {
                if(!response.errorBody().string().isEmpty())
                    completeCallback.onTokenFailure(new Throwable(response.errorBody().string()));
                else
                    completeCallback.onTokenFailure(new Throwable(response.raw().message()));
            } catch (IOException e) {
                e.printStackTrace();
                completeCallback.onTokenFailure(e);
            }
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    public void onFailure(Call<Token> call, Throwable t) {
        completeCallback.onTokenFailure(t);
    }

    /** Main Splash Caller */
    private class AsyncRunner extends RequestRunner<Void,Void> {

        private Callback<Token> callback;

        AsyncRunner(Callback<Token> callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            cache = new Cache(mContext);
        }

        @Override
        protected Void doInBackground(Void... nullified) {
            try {
                tokenInstance = cache.getCachedToken();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if(tokenInstance != null && tokenInstance.getExpires() > (System.currentTimeMillis()/1000L))
                completeCallback.onTokenSuccess();
            else {
                if(prefs.isAuthenticated()) {
                    AuthService.requestRefreshTokenAsync(callback, prefs.getRefresh());
                }
                else
                    AuthService.requestClientTokenAsync(callback);
            }

            return null;
        }
    }

    /** First Time User Login */
    private class TokenUpdaterAsync extends AsyncTask<String,Void,Boolean> {

        @Override
        protected void onPreExecute() {
            cache = new Cache(mContext);
        }

        @Override
        protected Boolean doInBackground(String... codes) {
            Token internalToken = AuthService.requestCodeTokenSync(codes[0]);
            try {
                if(internalToken != null) {
                    prefs.setRefresh(internalToken.getRefresh_token());
                    cache.saveTokenToCache(internalToken);
                    tokenInstance = internalToken;
                }
                else
                    return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    /*Interface with callback for operation failure or success*/
    public interface onTokenComplete {
        void onTokenSuccess();
        void onTokenFailure(Throwable cause);
    }
}