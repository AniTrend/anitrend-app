package com.mxt.anitrend.util;

import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.mxt.anitrend.service.JobDispatcherService;

/**
 * Created by Maxwell on 12/4/2016.
 * Schedules future services via job dispatcher
 */
public final class JobSchedulerUtil {

    private static final String JOB_DISPATCHER = "anitrend_job_service_18602_0589";

    /**
     * Gets the 45% of the current set sync time from preferences
     */
    private static int getMinimumSyncTime(ApplicationPref applicationPref) {
        return (int) (0.45 * applicationPref.getSyncTime());
    }

    /**
     * Schedules a new job service or replaces the existing job if one
     * exists.
     * @param context any valid application context
     */
    public static void scheduleJob(Context context) {
        ApplicationPref applicationPref = new ApplicationPref(context);
        if (applicationPref.isAuthenticated() && applicationPref.isNotificationEnabled()) {
            try {
                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
                Job syncJob = dispatcher.newJobBuilder()
                        // the JobService that will be called
                        .setService(JobDispatcherService.class)
                        // uniquely identifies the job
                        .setTag(JOB_DISPATCHER)
                        // recurring job
                        .setRecurring(true)
                        // The Job should be preserved until the next boot.
                        .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                        // start between 5 to e.g. XX minutes set in our sync preferences
                        .setTrigger(Trigger.executionWindow(getMinimumSyncTime(applicationPref), applicationPref.getSyncTime()))
                        // overwrite an existing job with the same tag
                        .setReplaceCurrent(true)
                        // retry with exponential backoff
                        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                        // constraints that need to be satisfied for the job to run
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .build();
                dispatcher.mustSchedule(syncJob);
                Log.d("JobSchedulerUtil", "JobDispatcher has been scheduled from context: "+context);
            } catch (FirebaseJobDispatcher.ScheduleFailedException ex) {
                Log.d("JobSchedulerUtil", ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        } else
            cancelJob(context);
    }

    /**
     * Cancels any scheduled job service.
     * @param context any valid application context
     */
    public static void cancelJob(Context context) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        switch (dispatcher.cancel(JOB_DISPATCHER)) {
            case FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS:
                Log.d("JobSchedulerUtil", "JobDispatcher has been canceled from context: "+context);
                break;
            case FirebaseJobDispatcher.CANCEL_RESULT_UNKNOWN_ERROR:
                Log.d("JobSchedulerUtil", "JobDispatcher failed to cancel for unknown reason "
                        +" second attempt to cancel all jobs :"+ dispatcher.cancelAll() + " from context: "+context );
                break;
            case FirebaseJobDispatcher.CANCEL_RESULT_NO_DRIVER_AVAILABLE:
                Log.d("JobSchedulerUtil", "JobDispatcher failed to cancel because no driver is available from context: "+context);
                break;
        }
    }
}
