package com.mxt.anitrend.presenter.index;

import android.app.Activity;
import android.content.Context;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.model.UserActivity;
import com.mxt.anitrend.base.custom.async.FeedPageFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import java.util.List;
import java.util.Locale;

import retrofit2.Callback;

/**
 * Created by max on 2017/03/09.
 */
public class UserActivityPresenter extends CommonPresenter<List<UserActivity>> {

    private final Context mContext;

    public UserActivityPresenter(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * For the current authenticated user
     */
    @Override
    public void beginAsync(Callback<List<UserActivity>> callback, int page, String type) {
        new FeedPageFetch(page, type, callback, mContext).execute();
    }

    /**
     * For a specific user
     */
    public void beginAsync(Callback<List<UserActivity>> callback, int page, String type, String user) {
        new FeedPageFetch(page, type, user, callback, mContext).execute();
    }


    public void showAlertTip() {
        if(getAppPrefs().getUserProfileTip()) {
            createAlerter((Activity) mContext, mContext.getString(R.string.title_new_features), mContext.getString(R.string.text_new_features),
                    R.drawable.ic_new_releases_white_24dp, R.color.colorStateBlue, 20);
            getAppPrefs().setUserProfileTip();
        }
    }

    public String getAnimeTime(int animeTime) {
        float item_time = animeTime / 60;
        if(item_time > 60) {
            item_time /= 24;
            if(item_time > 365)
                return mContext.getString(R.string.anime_time_years, item_time/365);
            return mContext.getString(R.string.anime_time_days, item_time);
        }
        return mContext.getString(R.string.anime_time_hours, item_time);
    }
}
