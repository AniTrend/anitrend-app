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
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.graphql.generated.FuzzyDateInput
import com.mxt.anitrend.graphql.generated.MediaListStatus
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
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

        val params = seriesManageBase.persistChanges()
        koinOf<WidgetMutationCoordinator>().saveMediaListEntry(
            id = params.intValue(KeyUtil.arg_id),
            mediaId = params.longValue(KeyUtil.arg_mediaId),
            status = params.enumValue<MediaListStatus>(KeyUtil.arg_listStatus),
            score = params.doubleValue(KeyUtil.arg_listScore),
            progress = params.intValue(KeyUtil.arg_listProgress),
            progressVolumes = params.intValue(KeyUtil.arg_listProgressVolumes),
            repeat = params.intValue(KeyUtil.arg_listRepeat),
            priority = params.intValue(KeyUtil.arg_listPriority),
            private = params.boolValue(KeyUtil.arg_listPrivate) ?: false,
            hiddenFromStatusLists = params.boolValue(KeyUtil.arg_listHiddenFromStatusLists) ?: false,
            customLists = params.stringListValue(KeyUtil.arg_listCustom),
            advancedScores = params.doubleListValue(KeyUtil.arg_listAdvancedScore),
            notes = params.stringValue(KeyUtil.arg_listNotes),
            startedAt = params.fuzzyDateInputValue(KeyUtil.arg_startedAt),
            completedAt = params.fuzzyDateInputValue(KeyUtil.arg_completedAt),
        ) { result ->
            try {
                progressDialog.dismiss()
                result
                    .onSuccess { mediaList ->
                        val modelClone = seriesManageBase.getModel().clone()
                        mediaList.media = modelClone.media
                        NotifyUtil
                            .makeText(
                                context,
                                context.getString(R.string.text_changes_saved),
                                R.drawable.ic_check_circle_white_24dp,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }.onFailure { throwable ->
                        Timber.e(throwable)
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

        val id = seriesManageBase.getModel().id
        koinOf<WidgetMutationCoordinator>().deleteMediaListEntry(id) { result ->
            try {
                progressDialog.dismiss()
                result
                    .onSuccess { deleteState ->
                        if (deleteState.isDeleted) {
                            NotifyUtil
                                .makeText(
                                    context,
                                    context.getString(R.string.text_changes_saved),
                                    R.drawable.ic_check_circle_white_24dp,
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    }.onFailure { throwable ->
                        Timber.w(throwable)
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

    @Suppress("DEPRECATION")
    private fun Bundle.value(key: String): Any? = if (containsKey(key)) get(key) else null

    private fun Bundle.intValue(key: String): Int? = value(key).asInt()

    private fun Bundle.longValue(key: String): Long? = when (val rawValue = value(key)) {
        is Number -> rawValue.toLong()
        is String -> rawValue.toLongOrNull()
        else -> null
    }

    private fun Bundle.stringValue(key: String): String? = value(key)?.toString()

    private fun Bundle.boolValue(key: String): Boolean? = when (val rawValue = value(key)) {
        is Boolean -> rawValue
        is String -> rawValue.toBooleanStrictOrNull()
        else -> null
    }

    private fun Bundle.doubleValue(key: String): Double? = when (val rawValue = value(key)) {
        is Number -> rawValue.toDouble()
        is String -> rawValue.toDoubleOrNull()
        else -> null
    }

    private fun Any?.asInt(): Int? = when (this) {
        is Number -> toInt()
        is String -> toIntOrNull()
        else -> null
    }

    private fun Bundle.stringListValue(key: String): List<String?>? = when (val rawValue = value(key)) {
        is Iterable<*> -> rawValue.map { it?.toString() }.takeIf { it.isNotEmpty() }
        is Array<*> -> rawValue.map { it?.toString() }.takeIf { it.isNotEmpty() }
        else -> rawValue?.toString()?.let(::listOf)
    }

    private fun Bundle.doubleListValue(key: String): List<Double?>? = when (val rawValue = value(key)) {
        is Iterable<*> -> rawValue.mapNotNull { valueItem ->
            when (valueItem) {
                is Number -> valueItem.toDouble()
                is String -> valueItem.toDoubleOrNull()
                else -> null
            }
        }.takeIf { it.isNotEmpty() }
        is DoubleArray -> rawValue.toList().takeIf { it.isNotEmpty() }
        is FloatArray -> rawValue.map { it.toDouble() }.takeIf { it.isNotEmpty() }
        else -> doubleValue(key)?.let(::listOf)
    }

    private fun Bundle.fuzzyDateInputValue(key: String): FuzzyDateInput? = when (val rawValue = value(key)) {
        is FuzzyDateInput -> rawValue
        is FuzzyDate -> rawValue.takeIf { it.isValidDate }?.let { date ->
            FuzzyDateInput(
                day = date.day,
                month = date.month,
                year = date.year,
            )
        }
        else -> null
    }

    private inline fun <reified T : Enum<T>> Bundle.enumValue(key: String): T? {
        val enumName = value(key)?.toString() ?: return null
        return runCatching { enumValueOf<T>(enumName) }
            .onFailure { Timber.tag(tagName).w(it, "Unknown %s value: %s", T::class.java.simpleName, enumName) }
            .getOrNull()
    }
}
