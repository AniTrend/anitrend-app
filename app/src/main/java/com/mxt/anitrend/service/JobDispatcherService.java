package com.mxt.anitrend.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.NotificationUtil;

import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Maxwell on 1/22/2017.
 */
public class JobDispatcherService extends Worker {

    private final BasePresenter presenter;

    public JobDispatcherService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        presenter = new BasePresenter(context);
    }

    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to <b>synchronously</b> do your work and return the
     * {@link Result} from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * {@link ListenableWorker}.
     * <p>
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * {@link Result}.  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The {@link Result} of the computation; note that
     * dependent work will not execute if you use
     * {@link Result#failure()}
     */
    @NonNull
    @Override
    public Result doWork() {
        if (presenter.getApplicationPref().isAuthenticated()) {
            UserModel userModel = WebFactory.createService(UserModel.class, getApplicationContext());
            Call<GraphContainer<User>> request = userModel.getCurrentUser(GraphUtil.getDefaultQuery(false));
            try {
                Response<GraphContainer<User>> response = request.execute();
                GraphContainer<User> userGraphContainer = response.body();
                if (response.isSuccessful() && userGraphContainer != null) {
                    User currentUser = userGraphContainer.getData().getResult();
                    User previousUserData = presenter.getDatabase().getCurrentUser();
                    presenter.getDatabase().saveCurrentUser(currentUser);
                    if (previousUserData.getUnreadNotificationCount() != currentUser.getUnreadNotificationCount()) {
                        if (currentUser.getUnreadNotificationCount() > 0) {
                            presenter.notifyAllListeners(new BaseConsumer<>(KeyUtil.USER_CURRENT_REQ, currentUser), false);
                            NotificationUtil.createNotification(getApplicationContext(), currentUser);
                        }
                    }
                    return Result.success();
                }
            } catch (Exception e) {
                Log.e(toString(), e.getMessage());
                e.printStackTrace();
            }
        }
        return Result.retry();
    }
}
