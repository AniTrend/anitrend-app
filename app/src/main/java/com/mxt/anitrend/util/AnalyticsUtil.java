package com.mxt.anitrend.util;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.crashlytics.android.Crashlytics;
import com.mxt.anitrend.App;

/**
 * Created by max on 2017/12/16.
 * Analytics helper
 */

public final class AnalyticsUtil {

    public static void logEvent(Context context, @NonNull String key, @NonNull Bundle params) {
        if(context != null)
            ((App) context).getAnalytics()
            .logEvent(key, params);
    }

    public static void logEvent(ApplicationPref applicationPref, Context context, @NonNull String key, @NonNull String params) {
        if (applicationPref.isAuthenticated() && context != null)
            ((App) context.getApplicationContext()).getAnalytics()
                    .setUserProperty(key, params);
    }

    public static void logCurrentScreen(FragmentActivity fragmentActivity, @NonNull String tag) {
        if(fragmentActivity != null)
            ((App) fragmentActivity.getApplicationContext()).getAnalytics()
                    .setCurrentScreen(fragmentActivity, tag, null);
    }

    public static void reportException(@NonNull String tag, @NonNull String message) {
        Crashlytics.log(0, tag, message);
    }

    public static void clearSession() {
        Crashlytics.setUserIdentifier("");
    }
}
