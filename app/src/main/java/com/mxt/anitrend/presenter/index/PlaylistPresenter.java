package com.mxt.anitrend.presenter.index;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.mxt.anitrend.api.call.Hub;
import com.mxt.anitrend.api.hub.Result;
import com.mxt.anitrend.base.custom.async.HubTaskFetch;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.util.KeyUtils;

import retrofit2.Callback;

/**
 * Created by max on 2017/08/12.
 */

public class PlaylistPresenter<T> extends CommonPresenter<Result<T>> {

    private Context context;
    private @KeyUtils.HubType int request;

    public PlaylistPresenter(Context context, @KeyUtils.HubType int request) {
        super(context);
        this.context = context;
        this.request = request;
    }

    @Override
    public void beginAsync(Callback<Result<T>> callback, int page) {
        Bundle bundle = new Bundle();
        bundle.putInt(Hub.arg_page, page);
        new HubTaskFetch<>(context, bundle, request, callback)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
