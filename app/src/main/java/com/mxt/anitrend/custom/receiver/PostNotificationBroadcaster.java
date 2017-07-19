package com.mxt.anitrend.custom.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.util.NotificationDispatcher;

import java.util.ArrayList;

/**
 * Created by Maxwell on 1/28/2017.
 * will call notification class
 */
public class PostNotificationBroadcaster extends BroadcastReceiver {

    //If you update this string make sure to do the same in the manifest
    public static final String BROADCAST = "com.mxt.receiver.PostNotificationBroadcaster";
    public static final String RESPONSE_KEY = "response_key";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive", intent.getAction());
        if(intent.getAction().equals(BROADCAST)) {
            ArrayList<UserNotification> response = intent.getParcelableArrayListExtra(RESPONSE_KEY);
            NotificationDispatcher.createNotification(context.getApplicationContext(), response);
        }
    }
}
