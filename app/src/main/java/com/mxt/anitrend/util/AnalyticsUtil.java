package com.mxt.anitrend.util;

import android.content.Context;
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
        if(fragmentActivity != null && new ApplicationPref(fragmentActivity).isUsageAnalyticsEnabled())
            ((App) fragmentActivity.getApplicationContext()).getAnalytics()
                    .setCurrentScreen(fragmentActivity, tag, null);
    }

    public static void reportException(@NonNull String tag, @NonNull String message) {
        Crashlytics.log(0, tag, message);
    }

    public static void clearSession() {
        Crashlytics.setUserIdentifier("");
    }

    public static void setCrashalyticsUser(Context context, String userName) {
        if(new ApplicationPref(context).isCrashReportsEnabled())
            Crashlytics.setUserIdentifier(userName);
    }
}
