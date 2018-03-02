package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesAnimeManage;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesManageBase;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesMangaManage;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.Series;
import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.general.SeriesList;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/01/20.
 * dialog utils for series entities
 */

final class SeriesDialogUtil extends DialogUtil {

    /**
     * General series managing template dialog builder which sets the text and icon based on the criteria,
     * new or old series entries.
     * <br/>
     *
     * @param context from a fragment activity derived class
     * @param model non-null series model object off or on the users list
     * @param isNewEntry represents the existence or absence of a series entity in the users list
     * @param title series title based on user preferences
     */
    static void createSeriesManage(Context context, @NonNull Series model, boolean isNewEntry, String title) {
        CustomSeriesManageBase seriesManageBase = buildManagerType(context, model.getSeries_type());
        seriesManageBase.setModel(model, isNewEntry);

        MaterialDialog.Builder materialBuilder = createSeriesManageDialog(context, isNewEntry, title);
        materialBuilder.customView(seriesManageBase, true);
        materialBuilder.onAny((dialog, which) -> {
            switch (which) {
                case POSITIVE:
                    onDialogPositive(context, seriesManageBase, dialog, isNewEntry);
                    break;
                case NEUTRAL:
                    dialog.dismiss();
                    break;
                case NEGATIVE:
                    onDialogNegative(context, seriesManageBase, dialog);
                    break;
            }
        });
        materialBuilder.show();
    }

    /**
     * General series managing template dialog builder which sets the text and icon based on the criteria,
     * new or old series entries.
     * <br/>
     *
     * @param context from a fragment activity derived class
     * @param model non-null series model object off or on the users list
     * @param isNewEntry represents the existence or absence of a series entity in the users list
     * @param title series title based on user preferences
     */
    static void createSeriesManage(Context context, @NonNull SeriesBase model, boolean isNewEntry, String title) {
        CustomSeriesManageBase seriesManageBase = buildManagerType(context, model.getSeries_type());
        seriesManageBase.setModel(model, isNewEntry);

        MaterialDialog.Builder materialBuilder = createSeriesManageDialog(context, isNewEntry, title);
        materialBuilder.customView(seriesManageBase, true);
        materialBuilder.onAny((dialog, which) -> {
            switch (which) {
                case POSITIVE:
                    onDialogPositive(context, seriesManageBase, dialog, isNewEntry);
                    break;
                case NEUTRAL:
                    dialog.dismiss();
                    break;
                case NEGATIVE:
                    onDialogNegative(context, seriesManageBase, dialog);
                    break;
            }
        });
        materialBuilder.show();
    }

    /**
     * General series managing template dialog builder which sets the text and icon based on the criteria,
     * new or old series entries.
     * <br/>
     *
     * @param context from a fragment activity derived class
     * @param model non-null series model object off or on the users list
     * @param isNewEntry represents the existence or absence of a series entity in the users list
     * @param title series title based on user preferences
     */
    static void createSeriesManage(Context context, @NonNull SeriesList model, boolean isNewEntry, String title) {
        SeriesBase seriesBase = model.getAnime() != null ? model.getAnime() : model.getManga();
        CustomSeriesManageBase seriesManageBase = buildManagerType(context, seriesBase.getSeries_type());
        seriesManageBase.setModel(model, isNewEntry);

        MaterialDialog.Builder materialBuilder = createSeriesManageDialog(context, isNewEntry, title);
        materialBuilder.customView(seriesManageBase, true);
        materialBuilder.onAny((dialog, which) -> {
            switch (which) {
                case POSITIVE:
                    onDialogPositive(context, seriesManageBase, dialog, isNewEntry);
                    break;
                case NEUTRAL:
                    dialog.dismiss();
                    break;
                case NEGATIVE:
                    onDialogNegative(context, seriesManageBase, dialog);
                    break;
            }
        });
        materialBuilder.show();
    }

    /**
     * Dialog negative or delete handler method
     * <br/>
     *
     * @param context from a fragment activity derived class
     */
    private static void onDialogPositive(Context context, CustomSeriesManageBase seriesManageBase, MaterialDialog dialog, boolean isNewEntry) {
        dialog.dismiss();

        ProgressDialog progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_processing_request);
        progressDialog.show();

        seriesManageBase.persistChanges();

        WidgetPresenter<SeriesList> presenter = new WidgetPresenter<>(context);
        presenter.setParams(seriesManageBase.getParam());

        @KeyUtils.RequestMode int requestMode = getRequestType(seriesManageBase.getModel(), isNewEntry);

        presenter.requestData(requestMode, context, new RetroCallback<SeriesList>() {
            @Override
            public void onResponse(@NonNull Call<SeriesList> call, @NonNull Response<SeriesList> response) {
                try {
                    SeriesList responseBody;
                    progressDialog.dismiss();
                    if(response.isSuccessful() && (responseBody = response.body()) != null) {
                        if(seriesManageBase.getModel().getAnime() != null)
                            responseBody.setAnime(seriesManageBase.getModel().getAnime());
                        else
                            responseBody.setManga(seriesManageBase.getModel().getManga());
                        presenter.getDatabase().getBoxStore(SeriesList.class).put(responseBody);
                        presenter.notifyAllListeners(new BaseConsumer<>(requestMode, responseBody), false);
                        NotifyUtil.makeText(context, context.getString(R.string.text_changes_saved), R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(this.toString(), ErrorUtil.getError(response));
                        NotifyUtil.makeText(context, context.getString(R.string.text_error_request), R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.toString(), e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SeriesList> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
                try {
                    progressDialog.dismiss();
                    NotifyUtil.makeText(context, context.getString(R.string.text_error_request), R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.toString(), e.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * Dialog negative or delete handler method
     * <br/>
     *
     * @param context from a fragment activity derived class
     */
    private static void onDialogNegative(Context context, CustomSeriesManageBase seriesManageBase, MaterialDialog dialog) {
        dialog.dismiss();

        ProgressDialog progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_processing_request);
        progressDialog.show();

        seriesManageBase.persistChanges();

        WidgetPresenter<ResponseBody> presenter = new WidgetPresenter<>(context);
        presenter.setParams(seriesManageBase.getParam());

        @KeyUtils.RequestMode int deleteType = seriesManageBase.getModel().getAnime() != null ?
                KeyUtils.ANIME_LIST_DELETE_REQ : KeyUtils.MANGA_LIST_DELETE_REQ;

        presenter.requestData(deleteType, context, new RetroCallback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    progressDialog.dismiss();
                    if(response.isSuccessful()) {
                        presenter.getDatabase().getBoxStore(SeriesList.class).remove(seriesManageBase.getModel());
                        presenter.notifyAllListeners(new BaseConsumer<>(deleteType, seriesManageBase.getModel()), false);
                        NotifyUtil.makeText(context, context.getString(R.string.text_changes_saved), R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(this.toString(), ErrorUtil.getError(response));
                        NotifyUtil.makeText(context, context.getString(R.string.text_error_request), R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                throwable.printStackTrace();
                try {
                    progressDialog.dismiss();
                    NotifyUtil.makeText(context, context.getString(R.string.text_error_request), R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @return the request type for a given series entry
     */
    private static @KeyUtils.RequestMode int getRequestType(SeriesList model, boolean isNewEntry) {
        if(model.getAnime() != null)
            return isNewEntry ? KeyUtils.ANIME_LIST_ADD_REQ : KeyUtils.ANIME_LIST_EDIT_REQ;
        else
            return isNewEntry ? KeyUtils.MANGA_LIST_ADD_REQ : KeyUtils.MANGA_LIST_EDIT_REQ;
    }

    /**
     * Creates manager view class for both anime and manga depending on
     * <br/>
     *
     * @param context from a fragment activity derived class
     */
    private static CustomSeriesManageBase buildManagerType(Context context, String seriesType) {
        if(seriesType.equals(KeyUtils.SeriesTypes[KeyUtils.ANIME]))
            return new CustomSeriesAnimeManage(context);
        return new CustomSeriesMangaManage(context);
    }

    /**
     * Dialog builder helper for series entities
     */
    private static MaterialDialog.Builder createSeriesManageDialog(Context context, boolean isNewEntry, String title) {
        MaterialDialog.Builder materialBuilder = createDefaultDialog(context)
                .icon(CompatUtil.getDrawableTintAttr(context, isNewEntry ? R.drawable.ic_fiber_new_white_24dp : R.drawable.ic_border_color_white_24dp, R.attr.colorAccent))
                .title(MarkDown.convert(context.getString(isNewEntry? R.string.dialog_add_title : R.string.dialog_edit_title, title)))
                .positiveText(isNewEntry? R.string.Add: R.string.Update)
                .neutralText(R.string.Cancel)
                .autoDismiss(false);
        if(!isNewEntry)
            materialBuilder.negativeText(R.string.Delete);
        return materialBuilder;
    }
}
