package com.mxt.anitrend;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.core.CrashlyticsCore;
import com.crashlytics.android.core.CrashlyticsListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.model.entity.MyObjectBox;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.LocaleHelper;

import org.greenrobot.eventbus.EventBus;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

/**
 * Created by max on 2017/10/22.
 * Application class
 */

public class App extends Application {

    private FirebaseAnalytics analytics;
    private BoxStore boxStore;
    private Fabric fabric;

    private void setupBoxStore() {
        boxStore = MyObjectBox.builder()
                .androidContext(App.this)
                .build();
        if(BuildConfig.DEBUG)
            new AndroidObjectBrowser(boxStore).start(this);
    }

    private void setCrashAnalytics(ApplicationPref pref) {
        CrashlyticsCore.Builder builder = new CrashlyticsCore.Builder();

        if (!BuildConfig.DEBUG)
            builder.disabled(pref.isCrashReportsEnabled());
        else
            builder.disabled(true);

        fabric = Fabric.with(new Fabric.Builder(this)
                .kits(builder.build())
                .debuggable(BuildConfig.DEBUG)
                .appIdentifier(BuildConfig.BUILD_TYPE)
                .build());
    }

    private void initApp(ApplicationPref pref) {
        EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(BuildConfig.DEBUG)
                .sendSubscriberExceptionEvent(BuildConfig.DEBUG)
                .throwSubscriberException(BuildConfig.DEBUG)
                .installDefaultEventBus();
        analytics = FirebaseAnalytics.getInstance(this);
        analytics.setAnalyticsCollectionEnabled(pref.isUsageAnalyticsEnabled());
        analytics.setMinimumSessionDuration(5000L);
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
        super.attachBaseContext(LocaleHelper.onAttach(base));
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
    public @NonNull Fabric getFabric() {
        return fabric;
    }

    /**
     * @return Application global registered firebase analytics
     *
     * @see com.mxt.anitrend.util.AnalyticsUtil
     */
    public @NonNull FirebaseAnalytics getAnalytics() {
        return analytics;
    }
}
