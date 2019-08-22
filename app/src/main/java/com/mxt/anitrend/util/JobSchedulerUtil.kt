package com.mxt.anitrend.util

import android.content.Context
import androidx.work.*
import com.mxt.anitrend.extension.appContext

import com.mxt.anitrend.service.JobDispatcherService

import java.util.concurrent.TimeUnit

/**
 * Created by Maxwell on 12/4/2016.
 * Schedules future services via job dispatcher
 */
object JobSchedulerUtil {

    private val constraints by lazy {
        Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
    }

    /**
     * Schedules a new job service or replaces the existing job if one exists.
     * @param context any valid application context
     */
    fun scheduleJob(context: Context) {
        val applicationPref = Settings(context)
        if (applicationPref.isAuthenticated && applicationPref.isNotificationEnabled) {
            val periodicWorkRequest = PeriodicWorkRequest.Builder(JobDispatcherService::class.java,
                    applicationPref.syncTime.toLong(), TimeUnit.MINUTES)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,
                            5, TimeUnit.MINUTES
                    )
                    .addTag(KeyUtil.WorkNotificationTag)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(KeyUtil.WorkNotificationId,
                            ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest)
        }
    }

    /**
     * Cancels any scheduled jobs.
     */
    fun cancelJob(context: Context = appContext) {
        WorkManager.getInstance(context).cancelUniqueWork(KeyUtil.WorkNotificationId)
    }
}
