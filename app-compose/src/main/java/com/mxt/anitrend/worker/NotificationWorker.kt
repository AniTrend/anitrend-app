package com.mxt.anitrend.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mxt.anitrend.data.local.dao.UserPreferencesDao
import com.mxt.anitrend.data.notification.NotificationRepository
import com.mxt.anitrend.util.NotificationUtil
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext

class NotificationWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val repository = GlobalContext.get().get<NotificationRepository>()
            val dao = GlobalContext.get().get<UserPreferencesDao>()

            val notifications = repository.observeNotifications().first()
            val prefs = dao.observe().first()
            val newCount = notifications.size
            val oldCount = prefs?.lastNotificationCount ?: 0
            val unreadCount = newCount - oldCount

            if (unreadCount > 0) {
                NotificationUtil.showNotification(
                    context = applicationContext,
                    title = "New Notifications",
                    body = "You have $unreadCount new notification${if (unreadCount > 1) "s" else ""}",
                    channelId = NotificationUtil.CHANNEL_NOTIFICATIONS,
                )
            }

            prefs?.let {
                dao.upsert(it.copy(lastNotificationCount = newCount))
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
