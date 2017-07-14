package com.mxt.anitrend.async;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.mxt.anitrend.api.call.CharacterModel;
import com.mxt.anitrend.api.call.ReviewModel;
import com.mxt.anitrend.api.call.SeriesModel;
import com.mxt.anitrend.api.call.StaffModel;
import com.mxt.anitrend.api.call.StudioModel;
import com.mxt.anitrend.api.call.UserListModel;
import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.util.KeyUtils;

import retrofit2.Call;
import retrofit2.Callback;

import static com.mxt.anitrend.api.service.ServiceGenerator.createService;
import static com.mxt.anitrend.util.KeyUtils.SeriesTypes;

/**
 * Created by max on 2017/04/06.
 * Universal async request runner
 */
@SuppressWarnings("unchecked")
public class AsyncTaskFetch <T> extends AsyncTask<AsyncTaskFetch.RequestType, Void, Call<T>> {

    // All request will probably need a callback and context
    private Callback<T> mCallback;
    private Context mContext;

    // Requests that may need an id
    private int id;
    private int page;
    private String term;

    public AsyncTaskFetch(Callback<T> mCallback, Context mContext) {
        this.mCallback = mCallback;
        this.mContext = mContext;
    }

    public AsyncTaskFetch(Callback<T> mCallback, Context mContext, int id) {
        this.mCallback = mCallback;
        this.mContext = mContext;
        this.id = id;
    }

    public AsyncTaskFetch(Callback<T> mCallback, Context mContext, String term) {
        this.mCallback = mCallback;
        this.mContext = mContext;
        this.term = term;
    }

    public AsyncTaskFetch(Callback<T> mCallback, Context mContext, String term, int page) {
        this.mCallback = mCallback;
        this.mContext = mContext;
        this.term = term;
        this.page = page;
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Call<T> doInBackground(@NonNull RequestType... params) {

        if(isCancelled())
            return null;

        // An exception will be thrown if you do not specify the request type
        switch (params[0]){
            case USER_CURRENT_REQ:
                return (Call<T>) createService(UserModel.class, mContext).fetchCurrentUser();
            case USER_ACCOUNT_REQ:
                if(id !=0)
                    return (Call<T>) createService(UserModel.class, mContext).fetchUser(id);
                return (Call<T>) createService(UserModel.class, mContext).fetchUser(term);
            case USER_FAVOURITES_REQ:
                return (Call<T>) createService(UserModel.class, mContext).fetchFavourites(id);
            case USER_FOLLOWERS_REQ:
                return (Call<T>) createService(UserModel.class, mContext).fetchFollowers(id);
            case USER_FOLLOWING_REQ:
                return (Call<T>) createService(UserModel.class, mContext).fetchFollowing(id);
            case USER_NOTIFICATION_COUNT:
                return (Call<T>) createService(UserModel.class, mContext).fetchNotificationCount();
            case USER_NOTIFICATION_REQ:
                return (Call<T>) createService(UserModel.class, mContext).fetchNotifications();
            case USER_ACTIVITY_REQ:
                return (Call<T>) createService(UserModel.class, mContext).fetchUserActivity(id);
            case USER_ANIME_LIST_REQ:
                return (Call<T>) createService(UserListModel.class, mContext).fetchAnimeList(id);
            case USER_MANGA_LIST_REQ:
                return (Call<T>) createService(UserListModel.class, mContext).fetchMangaList(id);
            case USER_SEARCH_REQ:
                return (Call<T>) createService(UserModel.class, mContext).searchUser(term);
            case CHARACTER_INFO_REQ:
                return (Call<T>) createService(CharacterModel.class, mContext).fetchCharacterPage(id);
            case CHARACTER_SEARCH_REQ:
                return (Call<T>) createService(CharacterModel.class, mContext).findChatacter(term);
            case STAFF_INFO_REQ:
                return (Call<T>) createService(StaffModel.class, mContext).fetchStaffPage(id);
            case ANIME_REVIEWS_REQ:
                return (Call<T>) createService(ReviewModel.class, mContext).fetchAnimeReviews(id);
            case MANGA_REVIEWS_REQ:
                return (Call<T>) createService(ReviewModel.class, mContext).fetchMangaReviews(id);
            case SERIES_REVIEWS_REQ:
                return (Call<T>) createService(ReviewModel.class, mContext).fetchSeriesReviews(term, page);
            case ANIME_SEARCH_REQ:
                return (Call<T>) ServiceGenerator.createService(SeriesModel.class, mContext).findSeries(SeriesTypes[KeyUtils.ANIME], term);
            case MANGA_SEARCH_REQ:
                return (Call<T>) ServiceGenerator.createService(SeriesModel.class, mContext).findSeries(SeriesTypes[KeyUtils.MANGA], term);
            case STUDIO_SEARCH_REQ:
                return (Call<T>) ServiceGenerator.createService(StudioModel.class, mContext).findStudio(term);
            case STUDIO_INFO_REQ:
                return (Call<T>) ServiceGenerator.createService(StudioModel.class, mContext).fetchStudioPage(id);
        }

        return null;
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param mCall The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Call<T> mCall) {
        if(mCall != null && !isCancelled())
            mCall.enqueue(mCallback);
    }

    public enum RequestType {
        USER_CURRENT_REQ,
        USER_ACCOUNT_REQ,
        USER_FAVOURITES_REQ,
        USER_FOLLOWERS_REQ,
        USER_FOLLOWING_REQ,
        USER_NOTIFICATION_COUNT,
        USER_NOTIFICATION_REQ,
        USER_ACTIVITY_REQ,
        USER_ANIME_LIST_REQ,
        USER_MANGA_LIST_REQ,
        USER_SEARCH_REQ,
        CHARACTER_INFO_REQ,
        CHARACTER_SEARCH_REQ,
        STAFF_INFO_REQ,
        ANIME_REVIEWS_REQ,
        MANGA_REVIEWS_REQ,
        SERIES_REVIEWS_REQ,
        ANIME_SEARCH_REQ,
        MANGA_SEARCH_REQ,
        STUDIO_SEARCH_REQ,
        STUDIO_INFO_REQ
    }
}