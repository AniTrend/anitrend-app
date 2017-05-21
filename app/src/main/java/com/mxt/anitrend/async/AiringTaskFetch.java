package com.mxt.anitrend.async;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.mxt.anitrend.api.call.UserModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.api.structure.ListItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/02/19.
 */

public class AiringTaskFetch extends AsyncTaskLoader<List<ListItem>> {

    private List<ListItem> mData;

    public AiringTaskFetch(Context context) {
        super(context);
    }

    /**
     * Subclasses must implement this to take care of resetting their loader,
     * as per {@link #reset()}.  This is not called by clients directly,
     * but as a result of a call to {@link #reset()}.
     * This will always be called from the process's main thread.
     */
    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if(mData != null)
            mData = null;
    }

    /**
     * Subclasses must implement this to take care of loading their data,
     * as per {@link #startLoading()}.  This is not called by clients directly,
     * but as a result of a call to {@link #startLoading()}.
     */
    @Override
    protected void onStartLoading() {
        if(mData != null) {
            deliverResult(mData);
        } else forceLoad();
    }

    /**
     * Subclasses must implement this to take care of stopping their loader,
     * as per {@link #stopLoading()}.  This is not called by clients directly,
     * but as a result of a call to {@link #stopLoading()}.
     * This will always be called from the process's main thread.
     */
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    /**
     * Called if the task was canceled before it was completed.  Gives the class a chance
     * to clean up post-cancellation and to properly dispose of the result.
     *
     * @param data The value that was returned by {@link #loadInBackground}, or null
     *             if the task threw {@link OperationCanceledException}.
     */
    @Override
    public void onCanceled(List<ListItem> data) {
        super.onCanceled(data);
        mData = data;
    }

    /**
     * Called on a worker thread to perform the actual load and to return
     * the result of the load operation.
     * <p>
     * Implementations should not deliver the result directly, but should return them
     * from this method, which will eventually end up calling {@link #deliverResult} on
     * the UI thread.  If implementations need to process the results on the UI thread
     * they may override {@link #deliverResult} and do so there.
     * <p>
     * To support cancellation, this method should periodically check the value of
     * {@link #isLoadInBackgroundCanceled} and terminate when it returns true.
     * Subclasses may also override {@link #cancelLoadInBackground} to interrupt the load
     * directly instead of polling {@link #isLoadInBackgroundCanceled}.
     * <p>
     * When the load is canceled, this method may either return normally or throw
     * {@link OperationCanceledException}.  In either case, the {@link Loader} will
     * call {@link #onCanceled} to perform post-cancellation cleanup and to dispose of the
     * result object, if any.
     *
     * @return The result of the load operation.
     * @throws OperationCanceledException if the load is canceled during execution.
     * @see #isLoadInBackgroundCanceled
     * @see #cancelLoadInBackground
     * @see #onCanceled
     */
    @Override
    public List<ListItem> loadInBackground() {
        UserModel userModel = ServiceGenerator.createService(UserModel.class, getContext());
        Call<List<ListItem>> data = userModel.fetchAiring(44);
        try {
            Response<List<ListItem>> response = data.execute();
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends the result of the load to the registered listener. Should only be called by subclasses.
     * <p>
     * Must be called from the process's main thread.
     *
     * @param data the result of the load
     */
    @Override
    public void deliverResult(List<ListItem> data) {
        mData = data;
        super.deliverResult(data);
    }
}
