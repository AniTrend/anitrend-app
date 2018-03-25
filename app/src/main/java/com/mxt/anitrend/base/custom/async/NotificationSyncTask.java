package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.service.JobDispatcherService;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.KeyUtils;

import java.io.IOException;

import retrofit2.Response;

/**
 * Created by max on 2017/12/11.
 * Part of the job dispatcher
 * @see JobDispatcherService
 */

public class NotificationSyncTask extends AsyncTask<Context, Void, User> {

    private static final String TAG = "NotificationSyncTask";
    private BasePresenter presenter;
    private User user;

    /**
     * Check the notification count for the current user
     * @return unread notification count
     */
    private void checkNotificationCount(UserModel userModel) {
        try {
            Response<User> response = userModel.getCurrentUser(GraphUtil.getDefaultQuery(false)).execute();
            if(response.isSuccessful() && (user = response.body()) != null) {
                DatabaseHelper databaseHelper = presenter.getDatabase();
                databaseHelper.saveCurrentUser(user);
            }
            else
                Log.e(TAG, ErrorUtil.getError(response));
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void onHandleInit(Context context) {
        presenter = new BasePresenter(context);
        if(presenter.getApplicationPref().isAuthenticated()) {
            UserModel requestModel = WebFactory.createService(UserModel.class, context);
            checkNotificationCount(requestModel);
        } else {
            JobSchedulerUtil.cancelJob(context);
            Log.e(TAG, "JobDispatcher has been unscheduled to avoid posting of notification while the user is not authenticated.");
        }
    }

    @Override
    protected @Nullable User doInBackground(Context... contexts) {
        Log.i("onStartJob", "JobDispatcher service has began execution "+contexts[0]);
        onHandleInit(contexts[0]);
        return user;
    }

    @Override
    protected void onPostExecute(@Nullable User user) {
        presenter.notifyAllListeners(new BaseConsumer<>(KeyUtils.USER_CURRENT_REQ, user), false);
        if(presenter != null)
            presenter.onDestroy();
    }
}
