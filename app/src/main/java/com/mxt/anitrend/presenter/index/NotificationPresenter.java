package com.mxt.anitrend.presenter.index;

import android.content.Context;

import com.mxt.anitrend.api.structure.UserNotification;
import com.mxt.anitrend.async.AsyncTaskFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.List;

import retrofit2.Callback;

/**
 * Created by Maxwell on 1/9/2017.
 */

public class NotificationPresenter extends CommonPresenter<List<UserNotification>> {

    private Context context;

    public NotificationPresenter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void beginAsync(Callback<List<UserNotification>> callback) {
        new AsyncTaskFetch<>(callback, context).execute(AsyncTaskFetch.RequestType.USER_NOTIFICATION_REQ);
    }
}
