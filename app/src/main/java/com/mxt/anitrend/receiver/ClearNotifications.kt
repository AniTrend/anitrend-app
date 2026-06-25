package com.mxt.anitrend.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings

class ClearNotifications : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        val extras = intent?.extras ?: return
        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE,
            ) as? NotificationManager
        if (extras.containsKey(KeyUtil.NOTIFICATION_ID)) {
            notificationManager?.cancel(extras.getInt(KeyUtil.NOTIFICATION_ID))
        }

        val settings = koinOf<Settings>()
        if (extras.containsKey(KeyUtil.NOTIFICATION_ID_REMOTE)) {
            settings.lastDismissedNotificationId = extras.getLong(KeyUtil.NOTIFICATION_ID_REMOTE)
        }

        when (extras.getString(KeyUtil.NOTIFICATION_ACTION)) {
            KeyUtil.NOTIFICATION_ACTION_DISMISS -> {
                if (!settings.clearNotificationOnDismiss) {
                    return
                }
            }
        }

        val scheduler = koinOf<JobSchedulerUtil>()
        scheduler.scheduleClearNotificationJob(context)
    }
}
