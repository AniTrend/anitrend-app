package com.mxt.anitrend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings

class DismissNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val settings = koinOf<Settings>()
        if (settings.clearNotificationOnDismiss) {
            val scheduler = koinOf<JobSchedulerUtil>()
            scheduler.scheduleClearNotificationJob(context)
        }
    }
}