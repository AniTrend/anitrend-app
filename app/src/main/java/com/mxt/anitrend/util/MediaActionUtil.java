package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import androidx.lifecycle.Lifecycle;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.LifecycleListener;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.model.entity.container.request.QueryContainerBuilder;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/01/05.
 * Media list action helper class is responsible for showing the correct dialog
 * for a given media
 */

public class MediaActionUtil implements RetroCallback<MediaBase>, LifecycleListener {

    private ProgressDialog progressDialog;
    private WidgetPresenter<MediaBase> presenter;
    private FragmentActivity context;

    private Lifecycle lifecycle;

    private long mediaId;

    MediaActionUtil(FragmentActivity context) {
        this.context = context;
        this.lifecycle = context.getLifecycle();
        this.presenter = new WidgetPresenter<>(context);
    }

    private void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    private void actionPicker() {
        MediaListOptions mediaListOptions = presenter.getDatabase().getCurrentUser().getMediaListOptions();

        // No need to add the parameter onList otherwise we'd have to handle an error code 404,
        // Instead we'd rather check if the the media has a non null mediaList item
        QueryContainerBuilder queryContainerBuilder = GraphUtil.INSTANCE.getDefaultQuery(false)
                .putVariable(KeyUtil.arg_id, mediaId)
                .putVariable(KeyUtil.arg_scoreFormat, mediaListOptions.getScoreFormat());

        presenter.getParams().putParcelable(KeyUtil.arg_graph_params, queryContainerBuilder);
        presenter.requestData(KeyUtil.MEDIA_WITH_LIST_REQ, context, this);
    }

    private void dismissProgress() {
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    public void startSeriesAction() {
        progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_checking_collection);
        progressDialog.show();
        actionPicker();
    }

    private void showActionDialog(@NonNull MediaBase mediaBase) {
        try {
            MediaDialogUtil.createSeriesManage(context, mediaBase);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.toString(), e.getLocalizedMessage());
        }
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    @Override
    public void onResponse(@NonNull Call<MediaBase> call, @NonNull Response<MediaBase> response) {
        if (lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            MediaBase mediaBase;
            if(response.isSuccessful() && (mediaBase = response.body()) != null) {
                showActionDialog(mediaBase);
            } else {
                Log.e(this.toString(), ErrorUtil.INSTANCE.getError(response));
                NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show();
            }
            dismissProgress();
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    @Override
    public void onFailure(@NonNull Call<MediaBase> call, @NonNull Throwable throwable) {
        if (lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            dismissProgress();
            throwable.printStackTrace();
            NotifyUtil.makeText(context, R.string.text_error_request, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Unregister any listeners from fragments or activities
     *
     * @param changeListener
     */
    @Override
    public void onPause(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(presenter != null)
            presenter.onPause(changeListener);
    }

    /**
     * Register any listeners from fragments or activities
     *
     * @param changeListener
     */
    @Override
    public void onResume(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(presenter != null)
            presenter.onResume(changeListener);
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    @Override
    public void onDestroy() {
        if(progressDialog != null)
            progressDialog.dismiss();
        if(presenter != null)
            presenter.onDestroy();
    }

    public static class Builder {

        private long mediaId;
        private MediaActionUtil mediaActionUtil;

        public Builder setId(long mediaId) {
            this.mediaId = mediaId;
            return this;
        }

        public MediaActionUtil build(FragmentActivity context) {
            mediaActionUtil = new MediaActionUtil(context);
            mediaActionUtil.setMediaId(mediaId);
            return mediaActionUtil;
        }
    }
}
