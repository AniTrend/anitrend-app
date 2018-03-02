package com.mxt.anitrend.base.custom.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener;
import com.mxt.anitrend.base.interfaces.event.LifecycleListener;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.model.entity.anilist.Genre;
import com.mxt.anitrend.model.entity.base.UserBase;
import com.mxt.anitrend.util.AnalyticsUtil;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.KeyUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by max on 2017/06/09.
 * Base presenter that will act as a template for all presenters
 * All preferences will be referenced from here.
 */

public abstract class CommonPresenter extends RecyclerScrollListener implements LifecycleListener {

    private AsyncTask asyncTask;

    private Bundle bundle;
    private Context context;
    private DatabaseHelper databaseHelper;
    private ApplicationPref applicationPref;
    
    public CommonPresenter(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public Bundle getParams() {
        if(bundle == null)
            bundle = new Bundle();
        return bundle;
    }

    public void setParams(Bundle bundle) {
        this.bundle = bundle;
    }

    public DatabaseHelper getDatabase() {
        if(databaseHelper == null)
            databaseHelper = new DatabaseHelper(context);
        return databaseHelper;
    }

    /**
     * Unregister any listeners from fragments or activities
     */
    @Override
    public void onPause(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(changeListener != null)
            getApplicationPref().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(changeListener);
    }

    /**
     * Register any listeners from fragments or activities
     */
    @Override
    public void onResume(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(changeListener != null)
            getApplicationPref().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(changeListener);
    }

    public AsyncTask getAsyncTask() {
        return asyncTask;
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    @Override
    public void onDestroy() {
        if(asyncTask != null && !asyncTask.isCancelled() && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
        bundle = null;
    }

    public ApplicationPref getApplicationPref() {
        if(applicationPref == null)
            applicationPref = new ApplicationPref(context);
        return applicationPref;
    }

    /**
     * Trigger all subscribers that may be listening. This method makes use of sticky broadcasts
     * in case all subscribed listeners were not loaded in time for the broadcast
     * <br/>
     *
     * @param param the object of type T to send
     * @param sticky set true to make sticky post
     */
    public <T> void notifyAllListeners(T param, boolean sticky) {
        if(sticky)
            EventBus.getDefault().postSticky(param);
        else
            EventBus.getDefault().post(param);
    }

    public interface AbstractPresenter <S extends CommonPresenter> {
        S getPresenter();
    }

    public boolean isCurrentUser(long userId) {
        return getApplicationPref().isAuthenticated() && getDatabase().getCurrentUser() != null &&
                userId != 0 && getDatabase().getCurrentUser().getId() == userId;
    }

    public boolean isCurrentUser(String userName) {
        return getApplicationPref().isAuthenticated() && getDatabase().getCurrentUser() != null &&
                userName != null && getDatabase().getCurrentUser().getDisplay_name().equals(userName);
    }

    public boolean isCurrentUser(long userId, String userName) {
        if (userName != null)
            return isCurrentUser(userName);
        return isCurrentUser(userId);
    }

    public boolean isCurrentUser(UserBase userBase) {
        return userBase != null && isCurrentUser(userBase.getId());
    }

    /**
     * Saves the selected genre indices
     */
    public void saveSelectedGenres(Integer[] selectedIndices, List<Genre> genres) {
        Set<String> geStringSet = new TreeSet<>();
        String genreStore = null;
        for (Integer index: selectedIndices) {
            String genre = genres.get(index).getGenre(); geStringSet.add(String.valueOf(index));
            if(TextUtils.isEmpty(genreStore)) genreStore = genre;
            else genreStore += "," + genre;
        }
        getApplicationPref().saveGenres(genreStore);
        getApplicationPref().saveGenresIndices(geStringSet);
        if(genreStore != null)
            AnalyticsUtil.logEvent(getApplicationPref(), getContext(), KeyUtils.arg_series_genres, genreStore);
    }

    /**
     * Gets all the selected genre indices
     */
    public Integer[] getSelectedGenres() {
        Set<String> genres = getApplicationPref().getGenresIndices();
        if(genres == null)
            return null;
        Integer[] selected = new Integer[genres.size()];
        int index = 0;
        for (String s: genres) {
            selected[index] = Integer.parseInt(s);
            index++;
        }
        return selected;
    }
}
