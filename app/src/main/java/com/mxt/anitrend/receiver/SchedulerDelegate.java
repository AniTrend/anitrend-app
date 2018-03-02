package com.mxt.anitrend.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mxt.anitrend.util.JobSchedulerUtil;

/**
 * Created by max on 2017/03/03.
 * broadcast receiver for scheduling or cancelling jobs
 */

public class SchedulerDelegate extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            JobSchedulerUtil.scheduleJob(context);
    }
}