package com.mxt.anitrend.base.custom.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DefaultPreferences;

/**
 * Created by Maxwell on 12/4/2016.
 * Schedules future services via job dispatcher
 */
public class ServiceScheduler {

    private FirebaseJobDispatcher dispatcher;
    private DefaultPreferences defaultPrefs;
    private ApplicationPrefs prefs;
    private Job syncJob;

    /**
     * Only call the constructor at the point of changes to avoid omitted changes
     * <br />
     *
     * @param context Current activity context or application context
     */
    public ServiceScheduler(@NonNull Context context) {
        prefs = new ApplicationPrefs(context);
        defaultPrefs = new DefaultPreferences(context);
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        syncJob = dispatcher.newJobBuilder()
                .setRecurring(true)
                .setReplaceCurrent(true)
                // the JobService that will be called
                .setService(JobDispatcherService.class)
                // uniquely identifies the job
                .setTag(JobDispatcherService.SERVICE_TAG)
                // recurring job
                .setRecurring(true)
                // persist past a device reboot
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                // start between 5 and e.g. 15 minutes (900 seconds)
                .setTrigger(Trigger.executionWindow(DefaultPreferences.MINIMUM_SYNC_TIME, defaultPrefs.getSyncTime()))
                // overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints( // only run on any network
                                 Constraint.ON_ANY_NETWORK)
                .build();
    }

    public void scheduleJob() {
        if (prefs.isAuthenticated() && defaultPrefs.isNotificationEnabled()) {
            try {
                Log.d("ServiceScheduler", "scheduleJob: has just been executed!");
                dispatcher.mustSchedule(syncJob);
            } catch (FirebaseJobDispatcher.ScheduleFailedException ex) {
                ex.printStackTrace();
            }
        }
        else
            Log.e(JobDispatcherService.SERVICE_TAG, "Current user is either not authenticated or JobScheduler is disabled");
    }

    public void cancelJob() {
        dispatcher.cancel(JobDispatcherService.SERVICE_TAG);
        Log.d("ServiceScheduler", "cancelJob(): has just been executed!");
    }
}
