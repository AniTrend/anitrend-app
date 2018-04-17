package com.mxt.anitrend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.container.body.GraphContainer;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.GraphUtil;

import retrofit2.Call;
import retrofit2.Response;

public class AuthenticatorService extends IntentService {

    private static final String ServiceName = "AuthenticatorService";

    public AuthenticatorService() {
        super(ServiceName);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        BasePresenter basePresenter = new BasePresenter(getApplicationContext());
        if(basePresenter.getApplicationPref().isAuthenticated()) {
            if(basePresenter.getDatabase().getCurrentUser() == null)
                WebFactory.createService(UserModel.class, getApplicationContext())
                        .getCurrentUser(GraphUtil.getDefaultQuery(false)).enqueue(new RetroCallback<GraphContainer<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<GraphContainer<User>> call, @NonNull Response<GraphContainer<User>> response) {
                        GraphContainer<User> graphContainer;
                        if(response.isSuccessful() && (graphContainer = response.body()) != null)
                            if(!graphContainer.isEmpty() && !graphContainer.getData().isEmpty())
                                basePresenter.getDatabase()
                                        .saveCurrentUser(graphContainer.getData().getResult());
                        else
                            ErrorUtil.getError(response);
                    }

                    @Override
                    public void onFailure(@NonNull Call<GraphContainer<User>> call, @NonNull Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        }
    }
}
