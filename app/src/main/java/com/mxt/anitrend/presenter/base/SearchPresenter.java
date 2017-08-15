package com.mxt.anitrend.presenter.base;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.mxt.anitrend.base.custom.async.AsyncTaskFetch;
import com.mxt.anitrend.presenter.CommonPresenter;

import retrofit2.Callback;

/**
 * Created by max on 2017/04/13.
 */
public class SearchPresenter<T> extends CommonPresenter<T> {

    private String search;
    private Context mContext;
    private AsyncTaskFetch.RequestType mRequestType;

    public SearchPresenter(Context mContext, @NonNull AsyncTaskFetch.RequestType mRequestType) {
        super(mContext);
        this.mContext = mContext;
        this.mRequestType = mRequestType;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    @Override
    public void beginAsync(Callback<T> callback) {
        if(mRequestType != null)
            new AsyncTaskFetch<>(callback, mContext, search).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mRequestType);
        else
            Toast.makeText(mContext, "The AsyncTaskFetch.RequestType was not set!", Toast.LENGTH_LONG).show();
    }

}
