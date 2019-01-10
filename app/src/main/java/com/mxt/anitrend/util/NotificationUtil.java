package com.mxt.anitrend.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mxt.anitrend.R;
import com.mxt.anitrend.model.entity.anilist.User;
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

    public static void createNotification(Context context, User userGraphContainer) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, KeyUtil.CHANNEL_TITLE)
                .setSmallIcon(R.drawable.ic_new_releases)
                .setSound(Uri.parse(getNotificationSound(context)))
                .setAutoCancel(true)
                .setPriority(PRIORITY_DEFAULT);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(KeyUtil.CHANNEL_TITLE, KeyUtil.CHANNEL_TITLE, importance);

            //we can optionally add a description for the channel
            String description = "A channel which shows notifications about events on AniTrend";
            channel.setDescription(description);

            //we can optionally set notification LED colour
            channel.setLightColor(Color.MAGENTA);

            // Register the channel with the system
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }

        int notificationCount = userGraphContainer.getUnreadNotificationCount();

        if(notificationCount > 0) {
            notificationBuilder.setContentIntent(multiContentIntent(context))
                    .setContentTitle(context.getString(R.string.alerter_notification_title))
                    .setContentText(context.getString(notificationCount > 1 ? R.string.text_notifications : R.string.text_notification, notificationCount));

            if(notificationManager != null)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
