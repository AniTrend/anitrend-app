package com.mxt.anitrend.base.custom.async;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.view.detail.fragment.YoutubeFragment;

/**
 * Created by max on 2017/03/04.
 */

public class YoutubeInitializer extends AsyncTaskLoader<YoutubeFragment> {

    private String youTube_id;
    private YoutubeFragment youtubeFragment;

    public YoutubeInitializer(Context context, String youTube_id) {
        super(context);
        this.youTube_id = youTube_id;
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if(youtubeFragment != null)
            youtubeFragment = null;
    }

    /**
     * Subclasses must implement this to take care of loading their data,
     * as per {@link #startLoading()}.  This is not called by clients directly,
     * but as a result of a call to {@link #startLoading()}.
     */
    @Override
    protected void onStartLoading() {
        /*if(youtubeFragment != null) {
            deliverResult(youtubeFragment);
        } else forceLoad();*/
        forceLoad();
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
    public void onCanceled(YoutubeFragment data) {
        super.onCanceled(data);
        if(data != null)
            youtubeFragment = data;
    }

    /**
     * Sends the result of the load to the registered listener. Should only be called by subclasses.
     * <p>
     * Must be called from the process's main thread.
     *
     * @param data the result of the load
     */
    @Override
    public void deliverResult(YoutubeFragment data) {
        youtubeFragment = data;
        super.deliverResult(data);
    }

    @Override
    public YoutubeFragment loadInBackground() {
        try {
            return YoutubeFragment.newInstance(youTube_id, BuildConfig.API_KEY);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
