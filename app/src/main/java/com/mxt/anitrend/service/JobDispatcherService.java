package com.mxt.anitrend.service;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;
import com.mxt.anitrend.base.custom.async.NotificationSyncTask;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.NotificationDispatcher;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Maxwell on 1/22/2017.
 * Force run a specific job for the current package:
 * adb shell cmd jobscheduler run -f com.mxt.anitrend Id
 *
 * Run all registered jobs for the current package:
 * adb shell dumpsys jobscheduler | grep com.mxt.anitrend
 */
public class JobDispatcherService extends JobService implements BaseConsumer.onRequestModelChange<User> {

    private JobParameters job;
    private NotificationSyncTask notificationSyncTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible. <br/>
     * When work is complete call jobFinished(job, true);
     * @param job
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(JobParameters job) {
        this.job = job;
        notificationSyncTask = new NotificationSyncTask();
        notificationSyncTask.execute(getApplicationContext());
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @param job
     *
     * @return whether the job should be retried
     *
     * @see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        if(notificationSyncTask != null)
            notificationSyncTask.cancel(true);
        Log.i("onStopJob", "JobDispatcher engine has decided to interrupt the execution of a running job");
        return true;
    }

    @Override @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onModelChanged(BaseConsumer<User> consumer) {
        if(consumer.getRequestMode() == KeyUtils.USER_CURRENT_REQ && consumer.getChangeModel() != null) {
            NotificationDispatcher.createNotification(getApplicationContext(), consumer.getChangeModel().getUnreadNotificationCount());
            jobFinished(job, false);
        } else
            jobFinished(job, true);
    }

    @Override
    public void onDestroy() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
