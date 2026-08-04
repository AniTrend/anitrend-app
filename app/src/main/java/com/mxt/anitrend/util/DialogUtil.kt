package com.mxt.anitrend.util

import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.text.SpannedString
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.util.markdown.MarkDownUtil

/**
 * Extension properties to replace MaterialDialog.selectedIndex/selectedIndices
 * on Android's AlertDialog (which backs MaterialAlertDialogBuilder).
 */
val DialogInterface.selectedIndex: Int
    get() = (this as? AlertDialog)?.listView?.checkedItemPosition ?: -1

val DialogInterface.selectedIndices: Array<Int>
    get() {
        val listView = (this as? AlertDialog)?.listView ?: return emptyArray()
        val checked = listView.checkedItemPositions ?: return emptyArray()
        val result = mutableListOf<Int>()
        for (i in 0 until checked.size()) {
            if (checked.valueAt(i)) {
                result.add(checked.keyAt(i))
            }
        }
        return result.toTypedArray()
    }

/**
 * Created by max on 2017/09/16.
 * Creates different dialog types
 */
object DialogUtil {
    @JvmStatic
    fun createDialogAttachMedia(
        @IdRes action: Int,
        editor: EditText,
        context: Context,
    ) {
        val inputField = EditText(context).apply {
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setHint(R.string.text_enter_text)
        }

        val builder = createDefaultDialog(context)

        when (action) {
            R.id.insert_link ->
                builder
                    .setTitle(R.string.attach_link_title)
                    .setMessage(context.getString(R.string.attach_link_text))
            R.id.insert_image ->
                builder
                    .setTitle(R.string.attach_image_title)
                    .setMessage(context.getString(R.string.attach_image_text))
            R.id.insert_youtube ->
                builder
                    .setTitle(R.string.attach_youtube_title)
                    .setMessage(context.getString(R.string.attach_youtube_text))
            R.id.insert_webm ->
                builder
                    .setTitle(R.string.attach_webm_title)
                    .setMessage(context.getString(R.string.attach_webm_text))
        }

        val processedAction = action

        val dialog =
            builder
                .setView(inputField)
                .setPositiveButton(R.string.Ok, null)
                .setNegativeButton(R.string.Cancel, null)
                .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (inputField.text.isNullOrBlank()) {
                NotifyUtil
                    .makeText(
                        context,
                        R.string.input_empty_warning,
                        Toast.LENGTH_SHORT,
                    ).show()
            } else {
                val text = inputField.text.toString()
                val start = editor.selectionStart
                when (processedAction) {
                    R.id.insert_link ->
                        editor.editableText.insert(start, MarkDownUtil.convertLink(text))
                    R.id.insert_image ->
                        editor.editableText.insert(start, MarkDownUtil.convertImage(text))
                    R.id.insert_youtube ->
                        editor.editableText.insert(start, MarkDownUtil.convertYoutube(text))
                    R.id.insert_webm ->
                        editor.editableText.insert(start, MarkDownUtil.convertVideo(text))
                }
                dialog.dismiss()
            }
        }
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            dialog.dismiss()
        }
    }

    @JvmStatic
    fun <T> createSelection(
        context: Context,
        @StringRes title: Int,
        selectedIndex: Int,
        selectableItems: Collection<T>,
        singleButtonCallback: DialogInterface.OnClickListener,
    ) {
        createDefaultDialog(context)
            .setTitle(title)
            .setSingleChoiceItems(
                selectableItems.map { it.toString() }.toTypedArray(),
                selectedIndex,
            ) { _, _ -> }
            .setPositiveButton(R.string.Ok, singleButtonCallback)
            .setNegativeButton(R.string.Cancel) { d, _ -> d.dismiss() }
            .show()
    }

    @JvmStatic
    fun createMessage(
        context: Context,
        @StringRes title: Int,
        @StringRes content: Int,
        singleButtonCallback: DialogInterface.OnClickListener,
    ) {
        createDefaultDialog(context)
            .setTitle(title)
            .setMessage(SpannedString(context.getString(content)))
            .setPositiveButton(R.string.Ok, singleButtonCallback)
            .setNegativeButton(R.string.Cancel) { d, _ -> d.dismiss() }
            .setIcon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .show()
    }

    @JvmStatic
    fun createMessage(
        context: Context,
        title: String,
        content: String,
    ) {
        createDefaultDialog(context)
            .setTitle(title)
            .setMessage(MarkDownUtil.convert(context, content))
            .setPositiveButton(R.string.Close) { d, _ -> d.dismiss() }
            .setIcon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .show()
    }

    @JvmStatic
    fun createMessage(
        context: Context,
        title: String,
        content: String,
        @StringRes positive: Int,
        @StringRes negative: Int,
        @StringRes neutral: Int,
        singleButtonCallback: DialogInterface.OnClickListener,
    ) {
        createDefaultDialog(context)
            .setTitle(title)
            .setMessage(MarkDownUtil.convert(context, content))
            .setPositiveButton(positive, singleButtonCallback)
            .setNegativeButton(negative) { d, _ -> d.dismiss() }
            .setNeutralButton(neutral) { d, _ -> d.dismiss() }
            .setIcon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .show()
    }

    @JvmStatic
    fun createMessage(
        context: Context,
        title: String,
        content: String,
        @StringRes positive: Int,
        @StringRes negative: Int,
        singleButtonCallback: DialogInterface.OnClickListener,
    ) {
        createDefaultDialog(context)
            .setTitle(title)
            .setMessage(MarkDownUtil.convert(context, content))
            .setPositiveButton(positive, singleButtonCallback)
            .setNegativeButton(negative) { d, _ -> d.dismiss() }
            .setIcon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .show()
    }

    @JvmStatic
    fun createTagMessage(
        context: Context,
        title: String,
        content: String,
        isSpoiler: Boolean?,
        @StringRes positive: Int,
        @StringRes negative: Int,
        singleButtonCallback: DialogInterface.OnClickListener,
    ) {
        val builder =
            createDefaultDialog(context)
                .setTitle(title)
                .setMessage(MarkDownUtil.convert(context, content))
                .setPositiveButton(positive, singleButtonCallback)
                .setNegativeButton(negative) { d, _ -> d.dismiss() }

        if (isSpoiler == true) {
            builder.setIcon(requireNotNull(context.getCompatDrawable(R.drawable.ic_spoiler_tag)))
        } else {
            builder.setIcon(requireNotNull(context.getCompatDrawable(R.drawable.ic_loyalty_white_24dp)))
        }

        builder.show()
    }

    @JvmStatic
    fun <T> createCheckList(
        context: Context,
        @StringRes title: Int,
        selectableItems: Collection<T>,
        selectedIndices: Array<Int>,
        listCallbackMultiChoice: DialogInterface.OnMultiChoiceClickListener,
        singleButtonCallback: DialogInterface.OnClickListener,
    ) {
        val items = selectableItems.map { it.toString() }.toTypedArray()
        val checkedItems = BooleanArray(items.size) { i -> i in selectedIndices }
        createDefaultDialog(context)
            .setTitle(title)
            .setMultiChoiceItems(items, checkedItems, listCallbackMultiChoice)
            .setPositiveButton(R.string.Ok, singleButtonCallback)
            .setNegativeButton(R.string.Reset) { d, _ -> d.dismiss() }
            .setNeutralButton(R.string.Cancel) { d, _ -> d.dismiss() }
            .setIcon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .show()
    }

    /**
     * Builds themed material dialog builder for basic configuration
     *
     * @param context from a fragment activity derived class
     * @see FragmentActivity
     */
    @JvmStatic
    fun createDefaultDialog(context: Context): MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        .setBackgroundInsetBottom(0)
        .setBackgroundInsetTop(0)
}
