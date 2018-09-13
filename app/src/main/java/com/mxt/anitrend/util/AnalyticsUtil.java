package com.mxt.anitrend.util;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.mxt.anitrend.App;

/**
 * Created by max on 2017/12/16.
 * Analytics helper
 */

public final class AnalyticsUtil {

    public static void logCurrentScreen(FragmentActivity fragmentActivity, @NonNull String tag) {
        if(fragmentActivity != null) {
            App app = ((App) fragmentActivity.getApplicationContext());
            if (app.getFabric() != null)
                app.getFabric().setCurrentActivity(fragmentActivity);
            if (app.getAnalytics() != null)
                app.getAnalytics().setCurrentScreen(fragmentActivity, tag, null);
        }
    }

    public static void reportException(@NonNull String tag, @NonNull String message) {
        Crashlytics.log(0, tag, message);
    }

    public static void clearSession() {
        Crashlytics.setUserIdentifier("");
    }

    public static void setCrashAnalyticsUser(FragmentActivity fragmentActivity, String userName) {
        if(fragmentActivity != null) {
            App app = ((App) fragmentActivity.getApplicationContext());
            if (app.getFabric() != null)
                Crashlytics.setUserIdentifier(userName);
        }
    }
}
