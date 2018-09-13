package com.mxt.anitrend.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.mxt.anitrend.R;
import com.mxt.anitrend.view.activity.detail.NotificationActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;

/**
 * Created by max on 1/22/2017.
 * NotificationUtil
 */

public final class NotificationUtil {

    private static final int NOTIFICATION_ID = 0x00000111;

    private static PendingIntent multiContentIntent(Context context){
        // PendingIntent.FLAG_UPDATE_CURRENT will update notification
        Intent activityStart = new Intent(context, NotificationActivity.class);
        return PendingIntent.getActivity(context, NOTIFICATION_ID, activityStart, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static String getNotificationSound(Context context) {
        ApplicationPref applicationPref = new ApplicationPref(context);
        return applicationPref.getNotificationsSound();
    }

    public static void createNotification(Context context, int notificationCount) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, KeyUtil.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_new_releases)
                .setSound(Uri.parse(getNotificationSound(context)))
                .setAutoCancel(true)
                .setPriority(PRIORITY_DEFAULT);

        if(notificationCount > 0) {
            notificationBuilder.setContentIntent(multiContentIntent(context))
                    .setContentTitle(context.getString(R.string.alerter_notification_title))
                    .setContentText(context.getString(notificationCount > 1 ? R.string.text_notifications : R.string.text_notification, notificationCount));

            android.app.Notification notification = notificationBuilder.build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null)
                notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
}
