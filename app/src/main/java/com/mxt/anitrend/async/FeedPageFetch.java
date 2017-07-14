package com.mxt.anitrend.async;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.ApplicationPrefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by max on 2017/03/09.
 */

public class FeedPageFetch extends AsyncTask<Void,Void,Call<List<UserActivity>>> {

    private int page;
    private String type;
    private String user;
    private Context mContext;
    private Callback<List<UserActivity>> callback;
    private final UserSmall current;

    public FeedPageFetch(int page, String type, Callback<List<UserActivity>> callback, Context mContext) {
        this.page = page;
        this.type = type;
        this.callback = callback;
        this.mContext = mContext;
        current = new ApplicationPrefs(mContext).getMiniUser();
    }

    public FeedPageFetch(int page, String type, String user, Callback<List<UserActivity>> callback, Context mContext) {
        this.page = page;
        this.type = type;
        this.user = user;
        this.callback = callback;
        this.mContext = mContext;
        current = new ApplicationPrefs(mContext).getMiniUser();
    }

    @Override
    protected Call<List<UserActivity>> doInBackground(Void... params) {
        UserModel userModel = ServiceGenerator.createService(UserModel.class, mContext);
        if(user == null) {
            if (!type.equals(KeyUtils.ActivtyTypes[KeyUtils.MESSAGE]))
                return userModel.fetchCurrentUserActivity(page, type);

            return userModel.fetchUserActivity(current.getDisplay_name(), page, type);
        } else {
            return userModel.fetchUserActivity(user, page, type);
        }
    }

    @Override
    protected void onPostExecute(Call<List<UserActivity>> mCall) {
        mCall.enqueue(callback);
    }
}
