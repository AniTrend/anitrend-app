
package com.mxt.anitrend.util

import android.content.Context
import androidx.work.*
import com.mxt.anitrend.service.ClearNotificationService

import com.mxt.anitrend.service.JobDispatcherService

import java.util.concurrent.TimeUnit

/**
 * Created by Maxwell on 12/4/2016.
 * Schedules future services via job dispatcher
 *
 * @param context any valid application context
 */
class JobSchedulerUtil(
    private val context: Context,
    private val settings: Settings
) {

    private fun getConstraints() =
        Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    /**
     * Schedules a new job service or replaces the existing job if one exists.
     */
    fun scheduleJob() {
        if (settings.isAuthenticated && settings.isNotificationEnabled) {
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                JobDispatcherService::class.java,
                settings.syncTime.toLong(),
                TimeUnit.MINUTES
            )
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        5,
                        TimeUnit.MINUTES
                    )
                    .addTag(KeyUtil.WorkNotificationTag)
                    .setConstraints(getConstraints())
                    .build()

            WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        KeyUtil.WorkNotificationId,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicWorkRequest
                    )
        }
    }

    fun scheduleClearNotificationJob() {
        if (settings.isAuthenticated && settings.clearNotificationOnDismiss) {
            val workRequest = OneTimeWorkRequest.Builder(
                ClearNotificationService::class.java
            )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5,
                    TimeUnit.MINUTES
                )
                .addTag(KeyUtil.WorkClearNotificationTag)
                .setConstraints(getConstraints())
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    KeyUtil.WorkClearNotificationId,
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
        }
    }

    /**
     * Cancels any scheduled jobs.
     */
    fun cancelJob() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(
                KeyUtil.WorkNotificationId
            )
    }
}
