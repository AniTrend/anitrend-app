package com.mxt.anitrend.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.mxt.anitrend.R;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.model.entity.anilist.Notification;
import com.mxt.anitrend.view.activity.detail.CommentActivity;
import com.mxt.anitrend.view.activity.detail.MediaActivity;
import com.mxt.anitrend.view.activity.detail.MessageActivity;
import com.mxt.anitrend.view.activity.detail.NotificationActivity;
import com.mxt.anitrend.view.activity.detail.ProfileActivity;

import java.util.List;

/**
 * Created by max on 1/22/2017.
 */

public final class NotificationDispatcher {

    private static final String NOTIFICATION_GROUP_KEY = "anitrend_notification_group";
    private static int NOTIFICATION_ID = 21;

    private static PendingIntent multiContentIntent(Context context){
        // PendingIntent.FLAG_UPDATE_CURRENT will update notification
        Intent activityStart = new Intent(context, NotificationActivity.class);
        return PendingIntent.getActivity(context, ++NOTIFICATION_ID, activityStart, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static String getNotificationSound(Context context) {
        ApplicationPref applicationPref = new ApplicationPref(context);
        return applicationPref.getNotificationsSound();
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null)
            notificationManager.cancelAll();
    }
    
    public static PendingIntent buildIntentLauncher(Context context, Notification notification) {
        Intent intent;
        switch (notification.getObject_type()) {
            case KeyUtils.NOTIFICATION_AIRING:
                intent = new Intent(context, MediaActivity.class);
                intent.putExtra(KeyUtils.arg_id, notification.getSeries().getId());
                intent.putExtra(KeyUtils.arg_mediaType, KeyUtils.SeriesTypes[KeyUtils.ANIME]);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case KeyUtils.NOTIFICATION_LIKE_ACTIVITY:
                intent = new Intent(context, CommentActivity.class);
                intent.putExtra(KeyUtils.arg_id, notification.getObject_id());
                break;
            case KeyUtils.NOTIFICATION_REPLY_ACTIVITY:
                intent = new Intent(context, CommentActivity.class);
                intent.putExtra(KeyUtils.arg_id, notification.getObject_id());
                break;
            case KeyUtils.NOTIFICATION_FOLLOW_ACTIVITY:
                intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(KeyUtils.arg_userName, notification.getUser().getName());
                break;
            case KeyUtils.NOTIFICATION_DIRECT_MESSAGE:
                intent = new Intent(context, MessageActivity.class);
                break;
            case KeyUtils.NOTIFICATION_LIKE_ACTIVITY_REPLY:
                intent = new Intent(context, CommentActivity.class);
                intent.putExtra(KeyUtils.arg_id, notification.getObject_id());
                break;
            case KeyUtils.NOTIFICATION_MENTION_ACTIVITY:
                intent = new Intent(context, CommentActivity.class);
                intent.putExtra(KeyUtils.arg_id, notification.getObject_id());
                break;
            default:
                intent = new Intent(context, NotificationActivity.class);
                break;
        }
        return PendingIntent.getActivity(context, ++NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void createNotification(Context context, List<Notification> notifications) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, KeyUtils.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_new_releases)
                .setSound(Uri.parse(getNotificationSound(context)))
                .setGroup(NOTIFICATION_GROUP_KEY)
                .setAutoCancel(true)
                .setPriority(android.app.Notification.PRIORITY_DEFAULT);

        if(notifications.size() > 1) {

            notificationBuilder.setContentIntent(multiContentIntent(context))
                    .setContentTitle(context.getString(R.string.alerter_notification_title))
                    .setContentText(context.getString(R.string.text_notifications, notifications.size()));

            android.app.Notification notification = notificationBuilder.build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null)
                notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            String url_load;
            Notification model = notifications.get(0);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_notification);
            remoteViews.setImageViewResource(R.id.notification_img, R.drawable.gradient_shadow);

            if(model.getObject_type() != KeyUtils.NOTIFICATION_AIRING) {
                url_load = model.getUser().getAvatar();
                switch (model.getObject_type()){
                    case KeyUtils.NOTIFICATION_COMMENT_FORUM:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getComment().getComment()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_comment_forum));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getComment().getComment()));
                        break;
                    case KeyUtils.NOTIFICATION_LIKE_FORUM:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_forum));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_LIKE_ACTIVITY:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_REPLY_ACTIVITY:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_reply_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_REPLY_FORUM:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getComment().getComment()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_reply_forum));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getComment().getComment()));
                        break;
                    case KeyUtils.NOTIFICATION_FOLLOW_ACTIVITY:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_follow_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_DIRECT_MESSAGE:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_activity_message));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_LIKE_ACTIVITY_REPLY:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_activity));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_MENTION_ACTIVITY:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_activity_mention));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    case KeyUtils.NOTIFICATION_LIKE_FORUM_COMMENT:
                        notificationBuilder.setContentTitle(model.getUser().getName());
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_user_like_comment));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                    default:
                        notificationBuilder.setContentTitle(context.getString(R.string.notification_default));
                        notificationBuilder.setContentText(String.format("%s", model.getMeta_value()));
                        remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_default));
                        if(model.getUser() != null)
                            remoteViews.setCharSequence(R.id.notification_header, "setText", model.getUser().getName());
                        else
                            remoteViews.setCharSequence(R.id.notification_header, "setText", "Unknown Origin");
                        remoteViews.setCharSequence(R.id.notification_content, "setText", String.format("%s", model.getMeta_value()));
                        break;
                }
            } else {
                url_load = model.getSeries().getImage_url_lge();

                notificationBuilder.setContentTitle(context.getString(R.string.notification_series));
                switch (new DatabaseHelper(context).getCurrentUser().getTitle_language()) {
                    case "romaji":
                        notificationBuilder.setContentText(context.getString(R.string.notification_episode_short, model.getMeta_value(), model.getSeries().getTitle_romaji()));
                        remoteViews.setCharSequence(R.id.notification_content, "setText", context.getString(R.string.notification_episode_short, model.getMeta_value(), model.getSeries().getTitle_romaji()));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getSeries().getTitle_romaji());
                        break;
                    case "english":
                        notificationBuilder.setContentText(context.getString(R.string.notification_episode_short, model.getMeta_value(), model.getSeries().getTitle_english()));
                        remoteViews.setCharSequence(R.id.notification_content, "setText", context.getString(R.string.notification_episode_short, model.getMeta_value(), model.getSeries().getTitle_english()));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getSeries().getTitle_english());
                        break;
                    case "japanese":
                        notificationBuilder.setContentText(context.getString(R.string.notification_episode_short, model.getMeta_value(), model.getSeries().getTitle_japanese()));
                        remoteViews.setCharSequence(R.id.notification_content, "setText", context.getString(R.string.notification_episode_short, model.getMeta_value(), model.getSeries().getTitle_japanese()));
                        remoteViews.setCharSequence(R.id.notification_header, "setText", model.getSeries().getTitle_japanese());
                        break;
                }
                remoteViews.setCharSequence(R.id.notification_subject, "setText", context.getString(R.string.notification_series));
            }

            notificationBuilder.setContentIntent(buildIntentLauncher(context, model))
                    .setCustomBigContentView(remoteViews);
            android.app.Notification notification = notificationBuilder.build();

            NotificationTarget notificationTarget = new NotificationTarget(context,
                    R.id.notification_img, remoteViews, notification, NOTIFICATION_ID);

            Glide.with(context).asBitmap().load(url_load).into(notificationTarget);
        }

    }
}
