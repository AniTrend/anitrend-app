
package com.mxt.anitrend.util

import android.content.Context
import androidx.work.*
import com.mxt.anitrend.worker.*

import java.util.concurrent.TimeUnit

/**
 * Created by Maxwell on 12/4/2016.
 * Schedules future services via job dispatcher
 *
 * @param context any valid application context
 */
class JobSchedulerUtil(private val settings: Settings) {

    private fun getConstraints() =
        Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun scheduleTagJob(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            TagSyncWorker::class.java,
            settings.syncTime.toLong() * 8,
            TimeUnit.MINUTES
        ).addTag(KeyUtil.WorkTagSyncId)
            .setConstraints(getConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                KeyUtil.WorkTagSyncId,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }

    fun scheduleGenreJob(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            GenreSyncWorker::class.java,
            settings.syncTime.toLong() * 8,
            TimeUnit.MINUTES
        ).addTag(KeyUtil.WorkGenreSyncId)
            .setConstraints(getConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                KeyUtil.WorkGenreSyncId,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }

    /**
     * Schedules a new job service or replaces the existing job if one exists.
     */
    fun scheduleNotificationJob(context: Context) {
        if (settings.isAuthenticated && settings.isNotificationEnabled) {
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                NotificationWorker::class.java,
                settings.syncTime.toLong(),
                TimeUnit.MINUTES
            ).setBackoffCriteria(
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

    fun scheduleClearNotificationJob(context: Context) {
        if (settings.isAuthenticated) {
            val workRequest = OneTimeWorkRequest.Builder(
                ClearNotificationWorker::class.java
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
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }

    fun startUpdateJob(context: Context) {
        val workRequest = OneTimeWorkRequest.Builder(
            UpdateWorker::class.java
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                5,
                TimeUnit.MINUTES
            )
            .addTag(KeyUtil.WorkUpdaterId)
            .setConstraints(getConstraints())
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                KeyUtil.WorkUpdaterId,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }

    /**
     * Cancels any scheduled jobs.
     */
    fun cancelNotificationJob(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(
                KeyUtil.WorkNotificationId
            )
    }

    /**
     * Cancels any scheduled jobs.
     */
    fun cancelTagJob(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(
                KeyUtil.WorkTagSyncId
            )
    }

    /**
     * Cancels any scheduled jobs.
     */
    fun cancelGenreJob(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(
                KeyUtil.WorkGenreSyncId
            )
    }
}
