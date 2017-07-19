package com.mxt.anitrend.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.api.call.UserListModel;
import com.mxt.anitrend.api.model.Series;
import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.api.structure.ListItem;
import com.mxt.anitrend.custom.UserListsCache;
import com.mxt.anitrend.custom.event.RemoteChangeListener;
import com.mxt.anitrend.util.ApplicationPrefs;
import com.mxt.anitrend.util.DialogManager;
import com.mxt.anitrend.util.ErrorHandler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Maxwell on 12/23/2016.
 * Handles fetching of records from the API endpoint and displaying
 * appropriate dialog for either updating or adding new item.
 * @see DialogManager for methods which will be called
 */
public class SeriesActionHelper extends AsyncTask<Void,Void,Void> {

    private Context context;
    private ApplicationPrefs applicationPrefs;
    private UserListsCache userListsCache;
    private ListItem userListItem;
    private @KeyUtils.SeriesType int mSeriesType;
    private ProgressDialog progressDialog;
    private Series series;
    private UserListModel seriesLists;
    private RemoteChangeListener mListener;
    private boolean isAutoIncrement;
    private final UserSmall mCurrent;

    public SeriesActionHelper(Context context, @KeyUtils.SeriesType int mSeriesType, Series series) {
        this.context = context;
        this.mSeriesType = mSeriesType;
        this.series = series;
        mCurrent = new ApplicationPrefs(context).getMiniUser();
    }

    public SeriesActionHelper(Context context, @KeyUtils.SeriesType int mSeriesType, ListItem userListItem) {
        this.context = context;
        this.mSeriesType = mSeriesType;
        this.userListItem = userListItem;
        mCurrent = new ApplicationPrefs(context).getMiniUser();
    }

    public SeriesActionHelper(Context context, @KeyUtils.SeriesType int mSeriesType, ListItem userListItem, RemoteChangeListener mListener, boolean isAutoIncrement) {
        this.context = context;
        this.userListItem = userListItem;
        this.mSeriesType = mSeriesType;
        this.mListener = mListener;
        this.isAutoIncrement = isAutoIncrement;
        mCurrent = new ApplicationPrefs(context).getMiniUser();
    }

    /**
     * Shows initial dialog for searching for items in your library
     */
    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.text_checking_collection));
        progressDialog.show();
    }

    /**
     * Creates required objects and requesting services
     */
    @Override
    protected Void doInBackground(Void... voids) {
        if(!isCancelled()) {
            applicationPrefs = new ApplicationPrefs(context);
            userListsCache = new UserListsCache();
            seriesLists = ServiceGenerator.createService(UserListModel.class, context);
        }
        return null;
    }

    /**
     *
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        if(!isCancelled()) {
            prepareTask();
        }
    }

    /**
     * Depending on the series type a temporary list cache is created which will be used to decide which action to take,
     * either adding a new item or editing!
     */
    private void prepareTask() {
        try {
            switch (mSeriesType) {
                case KeyUtils.ANIME:
                    seriesLists.fetchAnimeList(mCurrent.getId()).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if(response.isSuccessful() && response.body() != null) {
                                userListsCache.setAnimeLists(response.body().getLists());
                                new ActionHelper().execute();
                            } else {
                                Toast.makeText(context, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            t.printStackTrace();
                            if(t.getLocalizedMessage().startsWith("java.lang.IllegalStateException: Expected BEGIN_OBJECT")) {
                                new ActionHelper().execute();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    break;
                case KeyUtils.MANGA:
                    seriesLists.fetchMangaList(mCurrent.getId()).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            if(response.isSuccessful() && response.body() != null){
                                userListsCache.setMangaLists(response.body().getLists());
                                new ActionHelper().execute();
                            } else {
                                Toast.makeText(context, ErrorHandler.getError(response).toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            t.printStackTrace();
                            if(t.getLocalizedMessage().startsWith("java.lang.IllegalStateException: Expected BEGIN_OBJECT")) {
                                new ActionHelper().execute();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Async task that decides which dialog to show
     */
    private class ActionHelper extends AsyncTask<Void,Void,ListItem> {

        /**
         * Detects if the current anime items exists in the users list
         */
        @NonNull
        private Integer animeItems() {

            if(userListsCache.getAnimeLists() == null)
                return -1;

            int filter;
            if(series != null)
                filter = series.getId();
            else
                filter = userListItem.getSeries_id();
            int index = -1;
            List<ListItem> animeList = userListsCache.getAnimeLists();
            for (int i = 0; i < animeList.size(); i++) {
                if (animeList.get(i).getSeries_id() == filter) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        /**
         * Detects if the current manga items exists in the users list
         */
        @NonNull
        private Integer mangaItems() {

            if(userListsCache.getMangaLists() == null)
                return -1;

            int filter;
            if(series != null)
                filter = series.getId();
            else
                filter = userListItem.getSeries_id();
            int index = -1;

            List<ListItem> mangaLists = userListsCache.getMangaLists();
            for (int i = 0; i < mangaLists.size(); i++) {
                if (mangaLists.get(i).getSeries_id() == filter) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        @Override
        protected ListItem doInBackground(Void... voids) {
            Integer index;
            switch (mSeriesType) {
                case KeyUtils.ANIME:
                    if((index = animeItems()) != -1)
                        return userListsCache.getAnimeLists().get(index);
                    return null;
                case KeyUtils.MANGA:
                    if((index = mangaItems()) != -1)
                        return userListsCache.getMangaLists().get(index);
                    return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ListItem data) {
            try {
                // windows getting leaked, possibly when a crash occurs before the dialog is dismissed
                progressDialog.dismiss();
                completePost(data);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private void completePost(ListItem data) {
            DialogManager dialogManager = new DialogManager(context);
            if(data == null) switch (mSeriesType) {
                case KeyUtils.ANIME:
                    if(series != null)
                        dialogManager.animeAddDialogSmall(series);
                    else
                        dialogManager.animeAddDialogSmall(userListItem.getAnime());
                    break;
                case KeyUtils.MANGA:
                    if(series != null)
                        dialogManager.mangaAddDialogSmall(series);
                    else
                        dialogManager.mangaAddDialogSmall(userListItem.getManga());
                    break;
            }
            else switch (mSeriesType) {
                case KeyUtils.ANIME:
                    if(isAutoIncrement)
                        dialogManager.episodeAutoUpdate(data, mListener);
                    else
                        dialogManager.animeEditDialogSmall(data, mListener);
                    break;
                case KeyUtils.MANGA:
                    if(isAutoIncrement)
                        dialogManager.chapterAutoUpdate(data, mListener);
                    else
                        dialogManager.mangaEditDialogSmall(data, mListener);
                    break;
            }
        }
    }
}
