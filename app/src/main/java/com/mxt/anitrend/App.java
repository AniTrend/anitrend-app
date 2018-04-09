package com.mxt.anitrend;

import android.app.Application;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mxt.anitrend.base.custom.async.ThreadPool;
import com.mxt.anitrend.model.entity.MyObjectBox;

import io.fabric.sdk.android.Fabric;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import io.wax911.emojify.EmojiManager;

/**
 * Created by max on 2017/10/22.
 * Application class
 */

public class App extends Application {

    private FirebaseAnalytics analytics;
    private BoxStore boxStore;

    private void setupBoxStore() {
        boxStore = MyObjectBox.builder()
                .androidContext(App.this)
                .build();
        if(BuildConfig.DEBUG)
            new AndroidObjectBrowser(boxStore).start(this);
    }

    private void setCrashAnalytics() {
        Fabric.with(new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build());
    }

    private void initApp() {
        ThreadPool.Builder.create()
                .execute(() -> EmojiManager.initEmojiData(this));
        analytics = FirebaseAnalytics.getInstance(this);
        analytics.setAnalyticsCollectionEnabled(true);
        analytics.setMinimumSessionDuration(5000L);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setCrashAnalytics();
        setupBoxStore();
        initApp();
    }

    public @NonNull BoxStore getBoxStore() {
        return boxStore;
    }


    public @NonNull FirebaseAnalytics getAnalytics() {
        if(analytics == null)
            analytics = FirebaseAnalytics.getInstance(this);
        return analytics;
    }
}
