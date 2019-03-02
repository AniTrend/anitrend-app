package com.mxt.anitrend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.mxt.anitrend.util.JobSchedulerUtil

/**
 * Created by max on 2017/03/03.
 * broadcast receiver for scheduling or cancelling jobs
 */

class SchedulerDelegate : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == Intent.ACTION_BOOT_COMPLETED)
            JobSchedulerUtil.scheduleJob(context)
    }
}