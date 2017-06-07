package com.mxt.anitrend.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.receiver.PostNotificationBroadcaster;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 1/22/2017.
 * Job Scheduler Service
 */
public class JobDispatcherService extends JobService implements Callback<List<UserNotification>> {

    public static final String SERVICE_TAG = "PullService_ID789_108";
    private final static String name = "notification_collection_fetch_service";
    private JobParameters mJob;
    private int mNotificationCount;
    private PowerManager.WakeLock wakeLock;
    private volatile UserModel userModel;

    private Callback<Integer> notificationCountCallback = new Callback<Integer>() {

        @Override
        public void onResponse(Call<Integer> call, Response<Integer> response) {
            try {
                if(response.isSuccessful()) {
                    if (response.body() != 0) {
                        mNotificationCount = response.body();
                        Call<List<UserNotification>> request = userModel.fetchNotifications();
                        request.enqueue(JobDispatcherService.this);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                releaseWakeLockAndStop();
                jobFinished(mJob, true);
            }
        }

        @Override
        public void onFailure(Call<Integer> call, Throwable t) {
            try {
                Log.e("notificationCount", t.getLocalizedMessage());
                t.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                releaseWakeLockAndStop();
                jobFinished(mJob, true);
            }
        }
    };

    private void releaseWakeLockAndStop() {
        if(wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, name);
        wakeLock.acquire();
    }

    public JobDispatcherService() {
        super();
    }

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible. <br/>
     * When work is complete call jobFinished(job, true);
     * @param job
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters job) {
        Log.i("JOB_STARTED", SERVICE_TAG+": onStartJob() has just been executed!");
        try {
            mJob = job;
            acquireWakeLock();
            userModel = new AsyncTask<Void, Void, UserModel> () {
                @Override
                protected UserModel doInBackground(Void... params) {
                    if(isCancelled())
                        return null;

                    return ServiceGenerator.createService(UserModel.class, getApplicationContext());
                }
            }.execute().get();

            if(userModel != null) {
                Call<Integer> call = userModel.fetchNotificationCount();
                call.enqueue(notificationCountCallback);
                return true;
            }
        } catch (Exception e) {
            Log.e("onStartJob", e.getLocalizedMessage());
            e.printStackTrace();
            releaseWakeLockAndStop();
        }
        return false;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @param job
     *
     * @return whether the job should be retried
     *
     * @see Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        Log.i("JOB_STOPPED", "onStopJob() has just been executed!");
        return false;
    }

    @Override
    public void onResponse(Call<List<UserNotification>> call, @Nullable Response<List<UserNotification>> response) {
        try {
            if(response != null && response.isSuccessful()) {
                List<UserNotification> notifications = response.body();
                if(notifications != null && notifications.size() > 0) {
                    ArrayList<UserNotification> range_filter = new ArrayList<>(notifications.subList(0, mNotificationCount));
                    Intent intent = new Intent(PostNotificationBroadcaster.BROADCAST);
                    intent.putParcelableArrayListExtra(PostNotificationBroadcaster.RESPONSE_KEY, range_filter);
                    sendBroadcast(intent);
                } else {
                    Log.e("onHandleIntent - onPost", "notifications != null && notifications.size() > 0 returned true");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseWakeLockAndStop();
            jobFinished(mJob, true);
        }
    }

    @Override
    public void onFailure(Call<List<UserNotification>> call, Throwable t) {
        Log.e("JobDispatcher onFailure", t.getLocalizedMessage());
        t.printStackTrace();
        releaseWakeLockAndStop();
        jobFinished(mJob, true);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        notificationCountCallback = null;
        if(wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }
}
