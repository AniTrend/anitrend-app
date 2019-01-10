package com.mxt.anitrend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.model.entity.MyObjectBox;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.LocaleUtil;

import org.greenrobot.eventbus.EventBus;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

/**
 * Created by max on 2017/10/22.
 * Application class
 */

public class App extends MultiDexApplication {

    private @Nullable FirebaseAnalytics analytics;
    private BoxStore boxStore;
    private @Nullable Fabric fabric;

    private void setupBoxStore() {
        boxStore = MyObjectBox.builder()
                .androidContext(App.this)
                .build();
        if(BuildConfig.DEBUG)
            new AndroidObjectBrowser(boxStore).start(this);
    }

    private void setCrashAnalytics(ApplicationPref pref) {
        if (!BuildConfig.DEBUG)
            if (pref.isCrashReportsEnabled()) {
                CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                        .build();

                fabric = Fabric.with(new Fabric.Builder(this)
                        .kits(crashlyticsCore)
                        .appIdentifier(BuildConfig.BUILD_TYPE)
                        .build());
            }
    }

    private void initApp(ApplicationPref pref) {
        EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(BuildConfig.DEBUG)
                .sendSubscriberExceptionEvent(BuildConfig.DEBUG)
                .throwSubscriberException(BuildConfig.DEBUG)
                .installDefaultEventBus();
        if (pref.isUsageAnalyticsEnabled()) {
            analytics = FirebaseAnalytics.getInstance(this);
            analytics.setAnalyticsCollectionEnabled(pref.isUsageAnalyticsEnabled());
            analytics.setMinimumSessionDuration(5000L);
        }
        JobSchedulerUtil.scheduleJob(getApplicationContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationPref pref = new ApplicationPref(this);
        setCrashAnalytics(pref);
        setupBoxStore();
        initApp(pref);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtil.onAttach(base));
        MultiDex.install(this);
    }

    /**
     * @return Default application object box database instance
     *
     * @see com.mxt.anitrend.data.DatabaseHelper
     */
    public @NonNull BoxStore getBoxStore() {
        return boxStore;
    }

    /**
     * Get application global registered fabric instance, depending on
     * the current application preferences the application may have
     * disabled the current instance from sending any data
     *
     * @see com.mxt.anitrend.util.AnalyticsUtil
     */
    public @Nullable Fabric getFabric() {
        return fabric;
    }

    /**
     * @return Application global registered firebase analytics
     *
     * @see com.mxt.anitrend.util.AnalyticsUtil
     */
    public @Nullable FirebaseAnalytics getAnalytics() {
        return analytics;
    }
}
