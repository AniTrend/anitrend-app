package com.mxt.anitrend.initializer.scheduler

import android.content.Context
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.initializer.contract.AbstractInitializer
import com.mxt.anitrend.util.JobSchedulerUtil

class SchedulerInitializer : AbstractInitializer<Unit>() {

    /**
     * Initializes and a component given the application [Context]
     *
     * @param context The application context.
     */
    override fun create(context: Context) {
        val scheduler = koinOf<JobSchedulerUtil>()
        scheduler.scheduleNotificationJob(context)
        scheduler.scheduleGenreJob(context)
        scheduler.scheduleTagJob(context)
    }
}