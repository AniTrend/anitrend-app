package com.mxt.anitrend.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import com.mxt.anitrend.R
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.view.activity.detail.NotificationActivity
import org.koin.core.KoinComponent

/**
 * Created by max on 1/22/2017.
 * NotificationUtil
 */

class NotificationUtil(
        private val context: Context,
        private val settings: Settings,
        private val notificationManager: NotificationManager?
): KoinComponent {

    private var defaultNotificationId = 0x00000011

    private fun multiContentIntent(): PendingIntent {
        // PendingIntent.FLAG_UPDATE_CURRENT will update notification
        val targetActivity = Intent(
                context,
                NotificationActivity::class.java
        )
        return PendingIntent.getActivity(
                context,
                defaultNotificationId,
                targetActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun createNotification(userGraphContainer: User) {

        val notificationBuilder = NotificationCompat.Builder(context, KeyUtil.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_new_releases)
                .setSound(Uri.parse(settings.notificationsSound))
                .setAutoCancel(true)
                .setPriority(PRIORITY_HIGH)

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                //define the importance level of the notification
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                //build the actual notification channel, giving it a unique ID and name
                val channel = NotificationChannel(
                        KeyUtil.CHANNEL_ID, KeyUtil.CHANNEL_TITLE, importance
                ).apply {
                    //we can optionally add a description for the channel
                    description = "A channel which shows notifications about events on AniTrend"

                    //we can optionally set notification LED colour
                    lightColor = Color.MAGENTA
                }

                // Register the channel with the system
                notificationManager?.createNotificationChannel(channel)
            }
        }

        val notificationCount = userGraphContainer.unreadNotificationCount

        if (notificationCount > 0) {
            notificationBuilder.setContentIntent(multiContentIntent())
                    .setContentTitle(context.getString(R.string.alerter_notification_title))
                    .setContentText(context.getString(
                            when (notificationCount > 1) {
                                true -> R.string.text_notifications
                                else -> R.string.text_notification
                            }, notificationCount)
                    )

            defaultNotificationId = defaultNotificationId.inc()
            notificationManager?.notify(defaultNotificationId, notificationBuilder.build())
        }
    }
}
