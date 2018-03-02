package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.model.entity.general.Notification;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.service.JobDispatcherService;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

/**
 * Created by max on 2017/12/11.
 * Part of the job dispatcher
 * @see JobDispatcherService
 */

public class NotificationSyncTask extends AsyncTask<Context, Void, List<Notification>> {

    private static final String TAG = "NotificationSyncTask";
    private BasePresenter presenter;

    /**
     * Check the notification count for the current user
     * @return unread notification count
     */
    private Integer checkNotificationCount(UserModel userModel) {
        try {
            Response<Integer> response = userModel.getNotificationCount().execute();
            if(response.isSuccessful() && response.body() != null)
                return response.body();
            else
                Log.e(TAG, ErrorUtil.getError(response));
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private @Nullable List<Notification> getNewNotifications(UserModel userModel, Integer notificationCount) {
        List<Notification> notifications = null;
        try {
            List<Notification> responseBody;
            Response<List<Notification>> response = userModel.getNotifications().execute();
            if(response.isSuccessful() && (responseBody = response.body()) != null)
                notifications = responseBody.subList(0, notificationCount);
            else
                Log.e(TAG, ErrorUtil.getError(response));
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return notifications;
    }

    private void updateDatabase(@Nullable List<Notification> notifications, Integer notificationCount) {
        if(notifications != null) {
            DatabaseHelper databaseHelper = presenter.getDatabase();
            // check the database counter and make sure it is not more than 40, if so clear the db and set new notification count
        }
    }

    private List<Notification> onHandleInit(Context context) {
        presenter = new BasePresenter(context);
        if(presenter.getApplicationPref().isAuthenticated()) {
            UserModel requestModel = WebFactory.createService(UserModel.class, context);
            Integer notificationCount = checkNotificationCount(requestModel);
            if (notificationCount > 0) {
                requestModel = WebFactory.createService(UserModel.class, context);
                List<Notification> notifications = getNewNotifications(requestModel, notificationCount);
                updateDatabase(notifications, notificationCount);
                return notifications;
            }
        } else {
            JobSchedulerUtil.cancelJob(context);
            Log.e(TAG, "JobDispatcher has been unscheduled to avoid posting of notification while the user is not authenticated.");
        }
        return null;
    }

    @Override
    protected @Nullable List<Notification> doInBackground(Context... contexts) {
        Log.i("onStartJob", "JobDispatcher service has began execution "+contexts[0]);
        return onHandleInit(contexts[0]);
    }

    @Override
    protected void onPostExecute(@Nullable List<Notification> notifications) {
        presenter.notifyAllListeners(new BaseConsumer<>(KeyUtils.USER_NOTIFICATION_REQ, notifications), false);
        if(presenter != null)
            presenter.onDestroy();
    }
}
