package com.mxt.anitrend.util.media

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesAnimeManage
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesManageBase
import com.mxt.anitrend.base.custom.view.widget.CustomSeriesMangaManage
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.repository.BrowseMutation
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.apiError
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2018/01/20.
 * dialog utils for series entities
 */
internal object MediaDialogUtil {
    private val tagName = MediaDialogUtil::class.java.simpleName

    /**
     * General series managing template dialog builder which sets the text and icon based on the criteria,
     * new or old series entries.
     *
     * @param context from a fragment activity derived class
     * @param mediaBase non-null series model object off or on the users list
     */
    @JvmStatic
    fun createSeriesManage(
        context: Context,
        mediaBase: MediaBase,
    ) {
        val seriesType = mediaBase.type ?: KeyUtil.ANIME
        val seriesManageBase = buildManagerType(context, seriesType)
        seriesManageBase.setModel(mediaBase)
        val contentPadding = CompatUtil.dipToPx(16f)
        seriesManageBase.setPadding(contentPadding, contentPadding, contentPadding, contentPadding)

        val isNewEntry = mediaBase.mediaListEntry == null

        val dialog =
            createSeriesManageDialog(
                context,
                isNewEntry,
                MediaUtil.getMediaTitle(mediaBase),
            )
                .setView(seriesManageBase)
                .setPositiveButton(
                    if (isNewEntry) R.string.Add else R.string.Update,
                ) { d, _ ->
                    onDialogPositive(context, seriesManageBase, d as AlertDialog)
                }
                .setNeutralButton(R.string.Cancel) { d, _ -> d.dismiss() }
                .apply {
                    if (!isNewEntry) {
                        setNegativeButton(R.string.Delete) { d, _ ->
                            onDialogNegative(context, seriesManageBase, d as AlertDialog)
                        }
                    }
                }
                .show()
    }

    /**
     * Dialog negative or delete handler method
     */
    private fun onDialogPositive(
        context: Context,
        seriesManageBase: CustomSeriesManageBase,
        dialog: AlertDialog,
    ) {
        dialog.dismiss()

        val progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_processing_request)
        progressDialog.show()

        val presenter = koinOf<WidgetPresenter<MediaList>>()
        val params = seriesManageBase.persistChanges()
        presenter.params = params

        @KeyUtil.RequestType
        val requestType = KeyUtil.MUT_SAVE_MEDIA_LIST

        presenter.requestData(
            requestType,
            context,
            object : RetroCallback<MediaList> {
                override fun onResponse(
                    call: Call<MediaList>,
                    response: Response<MediaList>,
                ) {
                    try {
                        progressDialog.dismiss()
                        val modelClone = seriesManageBase.getModel().clone()
                        val responseBody = response.body()
                        if (response.isSuccessful && responseBody != null) {
                            responseBody.media = modelClone.media
                            koinOf<BrowseRepository>().emitMutationEvent(BrowseMutation.MediaListSaved(responseBody))
                            NotifyUtil
                                .makeText(
                                    context,
                                    context.getString(R.string.text_changes_saved),
                                    R.drawable.ic_check_circle_white_24dp,
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } else {
                            Timber.w(response.apiError())
                            NotifyUtil
                                .makeText(
                                    context,
                                    context.getString(R.string.text_error_request),
                                    R.drawable.ic_warning_white_18dp,
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    } catch (e: Exception) {
                        Timber.w(e)
                    }
                }

                override fun onFailure(
                    call: Call<MediaList>,
                    throwable: Throwable,
                ) {
                    Timber.e(throwable)
                    try {
                        progressDialog.dismiss()
                        NotifyUtil
                            .makeText(
                                context,
                                context.getString(R.string.text_error_request),
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            },
        )
    }

    /**
     * Dialog negative or delete handler method
     */
    private fun onDialogNegative(
        context: Context,
        seriesManageBase: CustomSeriesManageBase,
        dialog: AlertDialog,
    ) {
        dialog.dismiss()

        val progressDialog = NotifyUtil.createProgressDialog(context, R.string.text_processing_request)
        progressDialog.show()

        seriesManageBase.persistChanges()

        val presenter = koinOf<WidgetPresenter<DeleteState>>()
        val params: Bundle = seriesManageBase.persistChanges()
        presenter.params = params

        @KeyUtil.RequestType
        val requestType = KeyUtil.MUT_DELETE_MEDIA_LIST

        presenter.requestData(
            requestType,
            context,
            object : RetroCallback<DeleteState> {
                override fun onResponse(
                    call: Call<DeleteState>,
                    response: Response<DeleteState>,
                ) {
                    try {
                        progressDialog.dismiss()
                        val deleteState = response.body()
                        if (response.isSuccessful && deleteState != null) {
                            if (deleteState.isDeleted) {
                                koinOf<BrowseRepository>().emitMutationEvent(
                                    BrowseMutation.MediaListDeleted(seriesManageBase.getModel().id),
                                )
                                NotifyUtil
                                    .makeText(
                                        context,
                                        context.getString(R.string.text_changes_saved),
                                        R.drawable.ic_check_circle_white_24dp,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        } else {
                            Timber.w(response.apiError())
                            NotifyUtil
                                .makeText(
                                    context,
                                    context.getString(R.string.text_error_request),
                                    R.drawable.ic_warning_white_18dp,
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }

                override fun onFailure(
                    call: Call<DeleteState>,
                    throwable: Throwable,
                ) {
                    Timber.w(throwable)
                    try {
                        progressDialog.dismiss()
                        NotifyUtil
                            .makeText(
                                context,
                                context.getString(R.string.text_error_request),
                                R.drawable.ic_warning_white_18dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            },
        )
    }

    /**
     * Creates manager view class for both anime and manga depending on
     */
    private fun buildManagerType(
        context: Context,
        @KeyUtil.MediaType seriesType: String,
    ): CustomSeriesManageBase = if (seriesType == KeyUtil.ANIME) {
        CustomSeriesAnimeManage(context)
    } else {
        CustomSeriesMangaManage(context)
    }

    /**
     * Dialog builder helper for series entities
     */
    private fun createSeriesManageDialog(
        context: Context,
        isNewEntry: Boolean,
        title: String,
    ): MaterialAlertDialogBuilder {
        val materialBuilder =
            DialogUtil
                .createDefaultDialog(context)
                .setIcon(
                    context.getCompatTintedDrawable(if (isNewEntry) R.drawable.ic_fiber_new_white_24dp else R.drawable.ic_border_color_white_24dp),
                ).setTitle(
                    HtmlCompat.fromHtml(
                        context.getString(
                            if (isNewEntry) R.string.dialog_add_title else R.string.dialog_edit_title,
                            title,
                        ),
                        HtmlCompat.FROM_HTML_MODE_LEGACY,
                    ),
                )
        return materialBuilder
    }
}
