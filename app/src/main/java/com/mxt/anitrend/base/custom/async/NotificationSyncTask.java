package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.LifecycleListener;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.service.JobDispatcherService;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.JobSchedulerUtil;
import com.mxt.anitrend.util.KeyUtil;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/12/11.
 * Part of the job dispatcher
 * @see JobDispatcherService
 */

public class NotificationSyncTask implements RetroCallback<User>, LifecycleListener {

    private static final String TAG = "NotificationSyncTask";
    private WidgetPresenter<User> widgetPresenter;

    /**
     * Check the notification count for the current user
     */
    private void checkNotificationCount(Context context) {
        widgetPresenter.getParams().putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(false));
        widgetPresenter.requestData(KeyUtil.USER_CURRENT_REQ, context, this);
    }

    public void InitializeWith(Context context) {
        widgetPresenter = new WidgetPresenter<>(context);
        if(widgetPresenter.getApplicationPref().isAuthenticated())
            checkNotificationCount(context);
        else {
            JobSchedulerUtil.cancelJob(context);
            Log.e(TAG, "JobDispatcher has been unscheduled to avoid posting of notification while the user is not authenticated.");
        }
    }

    @Override
    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
        User user;
        if(response.isSuccessful() && (user = response.body()) != null) {
            widgetPresenter.getDatabase().saveCurrentUser(user);
            widgetPresenter.notifyAllListeners(new BaseConsumer<>(KeyUtil.USER_CURRENT_REQ, user), false);
        }
        else
            Log.e(TAG, ErrorUtil.getError(response));
    }

    @Override
    public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {
        if (!TextUtils.isEmpty(throwable.getMessage()))
            Log.e(TAG, throwable.getMessage());
        throwable.printStackTrace();
    }

    @Override
    public void onPause(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {

    }

    @Override
    public void onResume(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {

    }

    @Override
    public void onDestroy() {
        if(widgetPresenter != null)
            widgetPresenter.onDestroy();
    }
}
