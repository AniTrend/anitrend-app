package com.mxt.anitrend.presenter.detail;

import android.app.Activity;
import android.content.Context;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.Favourite;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.base.custom.async.AsyncTaskFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.List;

import retrofit2.Callback;

/**
 * Created by Maxwell on 11/25/2016.
 */
public class UserProfilePresenter extends CommonPresenter {

    private Context mContext;
    private int userId;

    public UserProfilePresenter(Context mContext, int userId) {
        super(mContext);
        this.mContext = mContext;
        this.userId = userId;
    }

    public void requestFavourites(Callback<Favourite> callback) {
        new AsyncTaskFetch<>(callback, mContext, userId).execute(AsyncTaskFetch.RequestType.USER_FAVOURITES_REQ);
    }

    public void requestFollowers(Callback<List<UserSmall>> callback) {
        new AsyncTaskFetch<>(callback, mContext, userId).execute(AsyncTaskFetch.RequestType.USER_FOLLOWERS_REQ);
    }

    public void requestFollowing(Callback<List<UserSmall>> callback) {
        new AsyncTaskFetch<>(callback, mContext, userId).execute(AsyncTaskFetch.RequestType.USER_FOLLOWING_REQ);
    }

    public void showAlertTip() {
        if(getAppPrefs().getUserProfileTip()) {
            createAlerter((Activity) mContext, mContext.getString(R.string.title_new_features), mContext.getString(R.string.text_new_features),
                    R.drawable.ic_new_releases_white_24dp, R.color.colorStateBlue, 20);
            getAppPrefs().setUserProfileTip();
        }
    }
}
