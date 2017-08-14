package com.mxt.anitrend.presenter.base;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.base.custom.async.AsyncTaskFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import retrofit2.Callback;

/**
 * Created by Maxwell on 11/20/2016.
 */

public class MyListPresenter extends CommonPresenter<User> {

    private Context mContext;
    private AsyncTaskFetch.RequestType mRequestType;

    public MyListPresenter(Context mContext, AsyncTaskFetch.RequestType mRequestType) {
        super(mContext);
        this.mContext = mContext;
        this.mRequestType = mRequestType;
    }

    @Override
    public void beginAsync(Callback<User> callback, int id) {
        new AsyncTaskFetch<>(callback, mContext, id).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mRequestType);
    }
}
