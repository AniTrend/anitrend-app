package com.mxt.anitrend.util

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mxt.anitrend.worker.NotificationWorker
import com.mxt.anitrend.worker.ScheduleWorker
import com.mxt.anitrend.worker.UpdateWorker
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    fun scheduleAll(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val notificationWork = PeriodicWorkRequestBuilder<NotificationWorker>(6, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "notification_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork,
        )

        val updateWork = PeriodicWorkRequestBuilder<UpdateWorker>(24, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "update_check",
            ExistingPeriodicWorkPolicy.KEEP,
            updateWork,
        )

        val scheduleWork = PeriodicWorkRequestBuilder<ScheduleWorker>(3, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "schedule_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            scheduleWork,
        )

        Log.d("WorkerScheduler", "All workers scheduled")
    }
}
