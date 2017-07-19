package com.mxt.anitrend.custom.event;

/**
 * Created by max on 2017/03/07.
 */
public interface NotificationClickListener {

    int TYPE_DIRECT_MESSAGE = 2;
    int TYPE_REPLY_ACTIVITY = 3;
    int TYPE_FOLLOW_ACTIVITY = 4;
    int TYPE_MENTION_ACTIVITY = 5;
    int TYPE_COMMENT_FORUM = 7;
    int TYPE_REPLY_FORUM = 8;
    int TYPE_AIRING = 9;
    int TYPE_LIKE_ACTIVITY = 10;
    int TYPE_LIKE_ACTIVITY_REPLY = 11;
    int TYPE_LIKE_FORUM = 12;
    int TYPE_LIKE_FORUM_COMMENT = 13;

    void onNotificationClick(int index);
}
