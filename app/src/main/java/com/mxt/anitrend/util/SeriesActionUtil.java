package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.LifecycleListener;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.general.SeriesList;
import com.mxt.anitrend.model.entity.general.SeriesList_;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/01/05.
 * Series list action helper class is responsible for showing the correct dialog
 * for a given series
 */

public class SeriesActionUtil implements RetroCallback<SeriesList>, LifecycleListener {

    private ProgressDialog progressDialog;
    private WidgetPresenter<SeriesList> presenter;
    private FragmentActivity context;

    private Lifecycle lifecycle;

    private Series series;
    private SeriesList seriesList;
    private SeriesBase seriesBase;

    SeriesActionUtil(FragmentActivity context) {
        this.context = context;
        this.lifecycle = context.getLifecycle();
        presenter = new WidgetPresenter<>(context);
    }

    private void setModels(Series series, SeriesList seriesList, SeriesBase seriesBase) {
        this.series = series;
        this.seriesList = seriesList;
        this.seriesBase = seriesBase;
    }

    private void actionPicker() {
        if(series != null) {
            presenter.getParams().putLong(KeyUtils.arg_id, series.getId());
            presenter.requestData(series.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]) ?
                            KeyUtils.ANIME_LIST_ITEM_REQ : KeyUtils.MANGA_LIST_ITEM_REQ, context, this);
        }
        else if (seriesBase != null) {
            presenter.getParams().putLong(KeyUtils.arg_id, seriesBase.getId());
            presenter.requestData(seriesBase.getSeries_type().equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]) ?
                    KeyUtils.ANIME_LIST_ITEM_REQ : KeyUtils.MANGA_LIST_ITEM_REQ, context, this);
        }
        else {
            presenter.getParams().putLong(KeyUtils.arg_id, seriesList.getSeries_id());
            presenter.requestData(seriesList.getAnime() != null ?
                    KeyUtils.ANIME_LIST_ITEM_REQ : KeyUtils.MANGA_LIST_ITEM_REQ, context, this);
        }
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

    private boolean isNewEntry(long seriesId) {
        return presenter.getDatabase().getBoxStore(SeriesList.class).query()
                .equal(SeriesList_.series_id, seriesId)
                .build().count() < 1;
    }

    private void showActionDialog() {
        try {
            if(series != null)
                SeriesDialogUtil.createSeriesManage(context, series, isNewEntry(series.getId()), SeriesUtil.getSeriesTitle(series, presenter.getLanguagePreference()));
            else if(seriesBase != null)
                SeriesDialogUtil.createSeriesManage(context, seriesBase, isNewEntry(seriesBase.getId()), SeriesUtil.getSeriesTitle(seriesBase, presenter.getLanguagePreference()));
            else
                SeriesDialogUtil.createSeriesManage(context, seriesList, isNewEntry(seriesList.getSeries_id()), SeriesUtil.getSeriesTitle(seriesList, presenter.getLanguagePreference()));
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
    public void onResponse(@NonNull Call<SeriesList> call, @NonNull Response<SeriesList> response) {
        if (lifecycle != null && lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            SeriesList seriesStatus;
            if(response.isSuccessful() && (seriesStatus = response.body()) != null) {
                if(!TextUtils.isEmpty(seriesStatus.getList_status()))
                    presenter.getDatabase().getBoxStore(SeriesList.class).put(seriesStatus);
                showActionDialog();
            } else {
                Log.e(this.toString(), ErrorUtil.getError(response));
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
    public void onFailure(@NonNull Call<SeriesList> call, @NonNull Throwable throwable) {
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

        private Series series;
        private SeriesList seriesList;
        private SeriesBase seriesBase;
        private SeriesActionUtil seriesActionUtil;

        public Builder setModel(Series series) {
            this.series = series;
            return this;
        }

        public Builder setModel(SeriesList seriesList) {
            this.seriesList = seriesList;
            return this;
        }

        public Builder setModel(SeriesBase seriesBase) {
            this.seriesBase = seriesBase;
            return this;
        }

        public SeriesActionUtil build(FragmentActivity context) {
            seriesActionUtil = new SeriesActionUtil(context);
            seriesActionUtil.setModels(series, seriesList, seriesBase);
            return seriesActionUtil;
        }
    }
}
