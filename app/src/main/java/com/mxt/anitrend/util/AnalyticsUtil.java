package com.mxt.anitrend.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.mxt.anitrend.App;

/**
 * Created by max on 2017/12/16.
 * Analytics helper
 */

public final class AnalyticsUtil {

    private static final String TAG = AnalyticsUtil.class.getSimpleName();

    public static void logCurrentScreen(FragmentActivity fragmentActivity, @NonNull String tag) {
        try {
            if(fragmentActivity != null) {
                App app = ((App) fragmentActivity.getApplicationContext());
                if (app.getFabric() != null)
                    app.getFabric().setCurrentActivity(fragmentActivity);
                if (app.getAnalytics() != null)
                    app.getAnalytics().setCurrentScreen(fragmentActivity, tag, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getLocalizedMessage() != null)
                Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public static void reportException(@NonNull String tag, @NonNull String message) {
        try {
            Crashlytics.log(0, tag, message);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getLocalizedMessage() != null)
                Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public static void clearSession() {
        try {
            Crashlytics.setUserIdentifier("");
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getLocalizedMessage() != null)
                Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public static void setCrashAnalyticsUser(FragmentActivity fragmentActivity, String userName) {
        try {
            if(fragmentActivity != null) {
                App app = ((App) fragmentActivity.getApplicationContext());
                if (app.getFabric() != null)
                    Crashlytics.setUserIdentifier(userName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getLocalizedMessage() != null)
                Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
