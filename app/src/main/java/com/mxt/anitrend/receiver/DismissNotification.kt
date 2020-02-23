package com.mxt.anitrend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import org.koin.core.KoinComponent
import org.koin.core.inject

class DismissNotification : BroadcastReceiver(), KoinComponent {

    private val scheduler by inject<JobSchedulerUtil>()

    private val settings by inject<Settings>()

    override fun onReceive(context: Context, intent: Intent?) {
        if (settings.clearNotificationOnDismiss) {
            scheduler.scheduleClearNotificationJob()
        }
    }
}