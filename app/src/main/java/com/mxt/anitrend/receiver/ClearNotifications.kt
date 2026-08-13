package com.mxt.anitrend.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ClearNotifications : KoinComponent, BroadcastReceiver() {

    private val settings by inject<Settings>()
    private val scheduler by inject<JobSchedulerUtil>()

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
        scheduler.scheduleClearNotificationJob(context)
    }
}
