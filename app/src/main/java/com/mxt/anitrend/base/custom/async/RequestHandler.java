package com.mxt.anitrend.base.custom.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.model.api.retro.WebFactory;
import com.mxt.anitrend.model.api.retro.anilist.BaseModel;
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel;
import com.mxt.anitrend.model.api.retro.anilist.MediaModel;
import com.mxt.anitrend.model.entity.container.request.GraphQueryContainer;
import com.mxt.anitrend.util.ApplicationPref;
import com.mxt.anitrend.util.KeyUtils;

import retrofit2.Call;
import retrofit2.Callback;

import static com.mxt.anitrend.util.KeyUtils.*;

/**
 * Created by max on 2017/09/16.
 * Handles all service creation for Retrofit Endpoints
 */

@SuppressWarnings("unchecked")
public class RequestHandler<T> extends AsyncTask<Context,Void,Call<T>> {

    private Bundle param;
    private Callback<T> callback;
    private @RequestMode int requestType;

    public RequestHandler(Bundle param, Callback<T> callback, int requestType) {
        this.param = param;
        this.callback = callback;
        this.requestType = requestType;
    }

    @Override
    protected Call<T> doInBackground(Context... contexts) {
        if(!isCancelled() && callback != null) {
            Context context = contexts[0];
            switch (requestType) {

                case GENRE_LIST_REQ:
                    return (Call<T>) WebFactory.createService(BaseModel.class, context).getGenres(param.getParcelable(arg_graph_params));
                case TAG_LIST_REQ:
                    return (Call<T>) WebFactory.createService(BaseModel.class, context).getTags(param.getParcelable(arg_graph_params));


                case EPISODE_FEED_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getRSS(param.getString(arg_search_query));
                case EPISODE_LATEST_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getLatestFeed();
                case EPISODE_POPULAR_REQ:
                    return (Call<T>) WebFactory.createCrunchyService(param.getBoolean(arg_feed), context).getPopularFeed();


                case GIPHY_SEARCH_REQ:
                    return (Call<T>) WebFactory.createGiphyService(context).findGif(BuildConfig.GIPHY_KEY, param.getString(arg_search_query),
                            PAGING_LIMIT, param.getInt(arg_page_offset), "PG", "en");
                case GIPHY_TRENDING_REQ:
                    return (Call<T>) WebFactory.createGiphyService(context).getTrending(BuildConfig.GIPHY_KEY, PAGING_LIMIT, param.getInt(arg_page_offset), "PG");


                case UPDATE_CHECKER_REQ:
                    if (param.containsKey(arg_branch_name))
                        return (Call<T>) WebFactory.createRepositoryService().checkVersion(param.getString(arg_branch_name));
                    return (Call<T>) WebFactory.createRepositoryService().checkVersion();

                case KeyUtils.MEDIA_BROWSE_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaBrowse(param.getParcelable(arg_graph_params));
                case KeyUtils.MEDIA_LIST_BROWSE_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaListBrowse(param.getParcelable(arg_graph_params));
                case KeyUtils.MEDIA_LIST_DELETE:
                    break;
                case KeyUtils.MEDIA_LIST_REQ:
                    return (Call<T>) WebFactory.createService(BrowseModel.class, context).getMediaList(param.getParcelable(arg_graph_params));
                case KeyUtils.MEDIA_LIST_SAVE:
                    break;
                case KeyUtils.MEDIA_LIST_UPDATE:
                    break;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Call<T> call) {
        if(!isCancelled() && call != null && callback != null)
            call.enqueue(callback);
    }

    @Override
    protected void onCancelled(Call<T> call) {
        if(call != null)
            call.cancel();
        callback = null;
        super.onCancelled(call);
    }

    public static GraphQueryContainer getDefaultQueryContainer() {
        return new GraphQueryContainer();
    }
}
