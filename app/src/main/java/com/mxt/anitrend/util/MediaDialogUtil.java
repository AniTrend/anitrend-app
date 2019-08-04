package com.mxt.anitrend.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.consumer.BaseConsumer;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesAnimeManage;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesManageBase;
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesMangaManage;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.model.entity.anilist.MediaList;
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState;
import com.mxt.anitrend.model.entity.base.MediaBase;
import com.mxt.anitrend.presenter.widget.WidgetPresenter;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2018/01/20.
 * dialog utils for series entities
 */

final class MediaDialogUtil extends DialogUtil {

    /**
     * General series managing template dialog builder which sets the text and icon based on the criteria,
     * new or old series entries.
     * <br/>
     *
     * @param context from a fragment activity derived class
     * @param mediaBase non-null series model object off or on the users list
     */
    static void createSeriesManage(Context context, @NonNull MediaBase mediaBase) {
        CustomSeriesManageBase seriesManageBase = buildManagerType(context, mediaBase.getType());
        seriesManageBase.setModel(mediaBase);

        boolean isNewEntry = mediaBase.getMediaListEntry() == null;

        MaterialDialog.Builder materialBuilder = createSeriesManageDialog(context, isNewEntry, MediaUtil.getMediaTitle(mediaBase));
        materialBuilder.customView(seriesManageBase, true);
        materialBuilder.onAny((dialog, which) -> {
            switch (which) {
                case POSITIVE:
                    onDialogPositive(context, seriesManageBase, dialog);
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
    private static void onDialogPositive(Context context, CustomSeriesManageBase seriesManageBase, MaterialDialog dialog) {
        dialog.dismiss();

        ProgressDialog progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_processing_request);
        progressDialog.show();

        WidgetPresenter<MediaList> presenter = new WidgetPresenter<>(context);
        Bundle params = seriesManageBase.persistChanges();
        presenter.setParams(params);

        @KeyUtil.RequestType int requestType = KeyUtil.MUT_SAVE_MEDIA_LIST;

        presenter.requestData(requestType, context, new RetroCallback<MediaList>() {
            @Override
            public void onResponse(@NonNull Call<MediaList> call, @NonNull Response<MediaList> response) {
                try {
                    MediaList responseBody;
                    progressDialog.dismiss();
                    final MediaList modelClone = seriesManageBase.getModel().clone();
                    if(response.isSuccessful() && (responseBody = response.body()) != null) {
                        responseBody.setMedia(modelClone.getMedia());
                        presenter.notifyAllListeners(new BaseConsumer<>(requestType, responseBody), false);
                        NotifyUtil.makeText(context, context.getString(R.string.text_changes_saved), R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(this.toString(), ErrorUtil.INSTANCE.getError(response));
                        NotifyUtil.makeText(context, context.getString(R.string.text_error_request), R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.toString(), e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MediaList> call, @NonNull Throwable throwable) {
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

        WidgetPresenter<DeleteState> presenter = new WidgetPresenter<>(context);
        Bundle params = seriesManageBase.persistChanges();
        presenter.setParams(params);

        @KeyUtil.RequestType int requestType = KeyUtil.MUT_DELETE_MEDIA_LIST;

        presenter.requestData(requestType, context, new RetroCallback<DeleteState>() {
            @Override
            public void onResponse(@NonNull Call<DeleteState> call, @NonNull Response<DeleteState> response) {
                try {
                    progressDialog.dismiss();
                    DeleteState deleteState;
                    if(response.isSuccessful() && (deleteState = response.body()) != null) {
                        if(deleteState.isDeleted()) {
                            presenter.notifyAllListeners(new BaseConsumer<>(requestType, seriesManageBase.getModel()), false);
                            NotifyUtil.makeText(context, context.getString(R.string.text_changes_saved), R.drawable.ic_check_circle_white_24dp, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(this.toString(), ErrorUtil.INSTANCE.getError(response));
                        NotifyUtil.makeText(context, context.getString(R.string.text_error_request), R.drawable.ic_warning_white_18dp, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteState> call, @NonNull Throwable throwable) {
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
     * Creates manager view class for both anime and manga depending on
     * <br/>
     *
     * @param context from a fragment activity derived class
     */
    private static CustomSeriesManageBase buildManagerType(Context context, @KeyUtil.MediaType String seriesType) {
        if(CompatUtil.INSTANCE.equals(seriesType, KeyUtil.ANIME))
            return new CustomSeriesAnimeManage(context);
        return new CustomSeriesMangaManage(context);
    }

    /**
     * Dialog builder helper for series entities
     */
    private static MaterialDialog.Builder createSeriesManageDialog(Context context, boolean isNewEntry, String title) {
        MaterialDialog.Builder materialBuilder = createDefaultDialog(context)
                .icon(CompatUtil.INSTANCE.getDrawableTintAttr(context, isNewEntry ? R.drawable.ic_fiber_new_white_24dp : R.drawable.ic_border_color_white_24dp, R.attr.colorAccent))
                .title(Html.fromHtml(context.getString(isNewEntry ? R.string.dialog_add_title : R.string.dialog_edit_title, title)))
                .positiveText(isNewEntry? R.string.Add: R.string.Update)
                .neutralText(R.string.Cancel)
                .autoDismiss(false);
        if(!isNewEntry)
            materialBuilder.negativeText(R.string.Delete);
        return materialBuilder;
    }
}
