package com.mxt.anitrend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AuthenticatorService extends IntentService {

    private static final String ServiceName = "AuthenticatorService";

    public AuthenticatorService() {
        super(ServiceName);
    }

    public void requestUserProfile() {
        final WidgetPresenter<User> widgetPresenter = new WidgetPresenter<>(getApplicationContext());
        widgetPresenter.getParams().putParcelable(KeyUtil.arg_graph_params, GraphUtil.getDefaultQuery(false));
        widgetPresenter.requestData(KeyUtil.USER_CURRENT_REQ, getApplicationContext(), new RetroCallback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                User user;
                if(response.isSuccessful() && (user = response.body()) != null) {
                    widgetPresenter.getDatabase().saveCurrentUser(user);
                    widgetPresenter.notifyAllListeners(new BaseConsumer<>(KeyUtil.USER_CURRENT_REQ, user), false);
                }
                else
                    Log.e(ServiceName, ErrorUtil.getError(response));
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {
                Log.e(ServiceName, throwable.getMessage());
                throwable.printStackTrace();
            }
        });
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        requestUserProfile();
    }
}
