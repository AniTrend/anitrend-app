package com.mxt.anitrend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.UserListModel;
import com.mxt.anitrend.model.api.retro.anilist.UserModel;
import com.mxt.anitrend.model.entity.anilist.Favourite;
import com.mxt.anitrend.model.entity.anilist.User;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.ErrorUtil;

import java.util.List;

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
            UserModel userModel = WebFactory.createService(UserModel.class, getApplicationContext());
            UserListModel userListModel = WebFactory.createService(UserListModel.class, getApplicationContext());
            User current = basePresenter.getDatabase().getCurrentUser();
            userModel.getFavourites(current.getId()).enqueue(new RetroCallback<Favourite>() {
                @Override
                public void onResponse(@NonNull Call<Favourite> call, @NonNull Response<Favourite> response) {
                    Favourite favourite;
                    if(response.isSuccessful() && (favourite = response.body()) != null) {
                        favourite.setId(current.getId());
                        basePresenter.getDatabase().saveFavourite(favourite);
                    } else
                        Log.e(ServiceName, ErrorUtil.getError(response));
                }

                @Override
                public void onFailure(@NonNull Call<Favourite> call, @NonNull Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

            userModel.getFollowing(current.getId()).enqueue(new RetroCallback<List<UserBase>>() {
                @Override
                public void onResponse(@NonNull Call<List<UserBase>> call, @NonNull Response<List<UserBase>> response) {
                    if(response.isSuccessful() && response.body() != null)
                        basePresenter.getDatabase().saveUsers(response.body());
                    else
                        Log.e(ServiceName, ErrorUtil.getError(response));
                }

                @Override
                public void onFailure(@NonNull Call<List<UserBase>> call, @NonNull Throwable throwable) {
                    throwable.printStackTrace();
                }
            });


            userListModel.getAnimeList(current.getId()).enqueue(new RetroCallback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    User user;
                    if(response.isSuccessful() && (user = response.body()) != null) {
                        basePresenter.getDatabase().saveCurrentUser(user);
                        if (user.getLists() != null)
                            basePresenter.getDatabase().saveSeries(user.getLists());
                        else
                            Log.e(ServiceName, ErrorUtil.getError(response));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {

                }
            });

            userListModel.getMangaList(current.getId()).enqueue(new RetroCallback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    User user;
                    if(response.isSuccessful() && (user = response.body()) != null) {
                        basePresenter.getDatabase().saveCurrentUser(user);
                        if (user.getLists() != null)
                            basePresenter.getDatabase().saveSeries(user.getLists());
                        else
                            Log.e(ServiceName, ErrorUtil.getError(response));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable throwable) {

                }
            });
        }
    }
}
