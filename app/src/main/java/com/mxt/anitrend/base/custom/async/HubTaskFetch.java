package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mxt.anitrend.api.call.Hub;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.util.KeyUtils;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by max on 2017/08/12.
 */

@SuppressWarnings("unchecked")
public class HubTaskFetch  <T> extends AsyncTask<Void, Void, Call<T>> {

    private Context context;
    private Bundle params;
    private Callback<T> callback;
    private @KeyUtils.HubType int request;

    public HubTaskFetch(Context context, @NonNull Bundle params, @KeyUtils.HubType int request, @NonNull Callback<T> callback) {
        this.context = context;
        this.params = params;
        this.request = request;
        this.callback = callback;
    }

    /**
     * Runs on the UI thread before {@link #doInBackground}.
     *
     * @see #onPostExecute
     * @see #doInBackground
     */
    @Override
    protected void onPreExecute() {
        params.putString(Hub.arg_order, "-dateAdded");
        params.putString(Hub.arg_expand, "users.addedBy");
        params.putString(Hub.arg_filter, Hub.filter);
        params.putString(Hub.arg_expand, "users.addedBy");
        params.putInt(Hub.arg_limit, 20);
        params.putString(Hub.arg_format, Hub.format);
    }

    @Override
    protected Call<T> doInBackground(Void... voids) {
        if(isCancelled())
            return null;
        Hub hub = ServiceGenerator.createHubService();
        switch (request) {
            case KeyUtils.FEED_TYPE:
                return (Call<T>) hub.getVideos(params.getString(Hub.arg_order), params.getInt(Hub.arg_limit),
                        params.getInt(Hub.arg_page), params.getString(Hub.arg_expand),
                        params.getString(Hub.arg_search), params.getString(Hub.arg_filter));
            case KeyUtils.PLAYLIST_TYPE:
                return (Call<T>) hub.getPlaylist(params.getString(Hub.arg_order), params.getInt(Hub.arg_limit),
                        params.getInt(Hub.arg_page), params.getString(Hub.arg_expand),
                        params.getString(Hub.arg_search), "{\"videos\":{\"$exists\":true}}");
            case KeyUtils.RSS_TYPE:
                return (Call<T>) hub.getRssFeed(params.getString(Hub.arg_order), params.getInt(Hub.arg_limit),
                        params.getInt(Hub.arg_page), params.getString(Hub.arg_expand),
                        params.getString(Hub.arg_search),  Hub.filter, params.getString(Hub.arg_format));
            case KeyUtils.VIDEO_TYPE:
                return (Call<T>) hub.getEpisode(params.getString(Hub.arg_id));
        }
        return null;
    }


    @Override
    protected void onPostExecute(Call<T> call) {
        if(call != null && !isCancelled())
            call.enqueue(callback);
    }
}
