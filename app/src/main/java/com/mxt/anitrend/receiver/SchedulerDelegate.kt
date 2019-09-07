package com.mxt.anitrend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.mxt.anitrend.util.JobSchedulerUtil
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by max on 2017/03/03.
 * broadcast receiver for scheduling or cancelling jobs
 */

class SchedulerDelegate : BroadcastReceiver(), KoinComponent {

    private val scheduler by inject<JobSchedulerUtil>()

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED ->
                scheduler.scheduleJob()
        }
    }
}