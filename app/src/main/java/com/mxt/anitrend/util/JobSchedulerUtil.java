package com.mxt.anitrend.util;

import android.content.Context;

import com.mxt.anitrend.service.JobDispatcherService;

import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

/**
 * Created by Maxwell on 12/4/2016.
 * Schedules future services via job dispatcher
 */
public final class JobSchedulerUtil {

    /**
     * Schedules a new job service or replaces the existing job if one exists.
     * @param context any valid application context
     */
    public static void scheduleJob(Context context) {
        ApplicationPref applicationPref = new ApplicationPref(context);
        if (applicationPref.isAuthenticated() && applicationPref.isNotificationEnabled()) {
            PeriodicWorkRequest periodicWorkRequest =
                    new PeriodicWorkRequest.Builder(JobDispatcherService.class,
                            applicationPref.getSyncTime(), TimeUnit.MINUTES)
                            .addTag(KeyUtil.WorkTag)
                            .build();

            WorkManager.getInstance()
                    .enqueueUniquePeriodicWork(KeyUtil.UniqueWorkId,
                            ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
        }
    }

    /**
     * Cancels any scheduled jobs.
     */
    public static void cancelJob() {
        WorkManager.getInstance().cancelUniqueWork(KeyUtil.UniqueWorkId);
    }
}
