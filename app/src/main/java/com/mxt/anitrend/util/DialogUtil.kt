package com.mxt.anitrend.util

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.text.SpannedString
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.markdown.MarkDownUtil
import timber.log.Timber
import java.io.IOException

/**
 * Created by max on 2017/09/16.
 * Creates different dialog types
 */
object DialogUtil {

    @JvmStatic
    fun createDialogAttachMedia(@IdRes action: Int, editor: EditText, context: Context) {
        val builder = createDefaultDialog(context)
            .positiveText(R.string.Ok)
            .negativeText(R.string.Cancel)
            .autoDismiss(false)
            .inputType(
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
            )
            .input(context.getString(R.string.text_enter_text), null) { _, _ ->
                // on input
            }

        when (action) {
            R.id.insert_link -> builder.title(R.string.attach_link_title)
                .content(R.string.attach_link_text)
                .onAny { dialog, which ->
                    when (which) {
                        DialogAction.POSITIVE -> {
                            val editText = dialog.getInputEditText()
                            if (editText != null) {
                                if (!TextUtils.isEmpty(editText.text)) {
                                    val start = editor.selectionStart
                                    editor.editableText.insert(
                                        start,
                                        MarkDownUtil.convertLink(editText.text.toString())
                                    )
                                    dialog.dismiss()
                                } else {
                                    NotifyUtil.makeText(
                                        context,
                                        R.string.input_empty_warning,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        DialogAction.NEUTRAL -> Unit
                        DialogAction.NEGATIVE -> dialog.dismiss()
                    }
                }
            R.id.insert_image -> builder.title(R.string.attach_image_title)
                .content(R.string.attach_image_text)
                .onAny { dialog, which ->
                    when (which) {
                        DialogAction.POSITIVE -> {
                            val editText = dialog.getInputEditText()
                            if (editText != null) {
                                if (!TextUtils.isEmpty(editText.text)) {
                                    val start = editor.selectionStart
                                    editor.editableText.insert(
                                        start,
                                        MarkDownUtil.convertImage(editText.text.toString())
                                    )
                                    dialog.dismiss()
                                } else {
                                    NotifyUtil.makeText(
                                        context,
                                        R.string.input_empty_warning,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        DialogAction.NEUTRAL -> Unit
                        DialogAction.NEGATIVE -> dialog.dismiss()
                    }
                }
            R.id.insert_youtube -> builder.title(R.string.attach_youtube_title)
                .content(R.string.attach_youtube_text)
                .onAny { dialog, which ->
                    when (which) {
                        DialogAction.POSITIVE -> {
                            val editText = dialog.getInputEditText()
                            if (editText != null) {
                                if (!TextUtils.isEmpty(editText.text)) {
                                    val start = editor.selectionStart
                                    editor.editableText.insert(
                                        start,
                                        MarkDownUtil.convertYoutube(editText.text.toString())
                                    )
                                    dialog.dismiss()
                                } else {
                                    NotifyUtil.makeText(
                                        context,
                                        R.string.input_empty_warning,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        DialogAction.NEUTRAL -> Unit
                        DialogAction.NEGATIVE -> dialog.dismiss()
                    }
                }
            R.id.insert_webm -> builder.title(R.string.attach_webm_title)
                .content(R.string.attach_webm_text)
                .onAny { dialog, which ->
                    when (which) {
                        DialogAction.POSITIVE -> {
                            val editText = dialog.getInputEditText()
                            if (editText != null) {
                                if (!TextUtils.isEmpty(editText.text)) {
                                    val start = editor.selectionStart
                                    editor.editableText.insert(
                                        start,
                                        MarkDownUtil.convertVideo(editText.text.toString())
                                    )
                                    dialog.dismiss()
                                } else {
                                    NotifyUtil.makeText(
                                        context,
                                        R.string.input_empty_warning,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        DialogAction.NEUTRAL -> Unit
                        DialogAction.NEGATIVE -> dialog.dismiss()
                    }
                }
        }
        builder.show()
    }

    @JvmStatic
    fun <T> createSelection(
        context: Context,
        @StringRes title: Int,
        selectedIndex: Int,
        selectableItems: Collection<T>,
        singleButtonCallback: MaterialDialog.SingleButtonCallback
    ) {
        createDefaultDialog(context)
            .title(title)
            .items(selectableItems)
            .positiveText(R.string.Ok)
            .negativeText(R.string.Cancel)
            .itemsCallbackSingleChoice(selectedIndex) { _, _, _, _ -> false }
            .autoDismiss(true)
            .onAny(singleButtonCallback)
            .show()
    }

    @JvmStatic
    fun createMessage(
        context: Context,
        @StringRes title: Int,
        @StringRes content: Int,
        singleButtonCallback: MaterialDialog.SingleButtonCallback
    ) {
        createDefaultDialog(context)
            .title(title)
            .positiveText(R.string.Ok)
            .negativeText(R.string.Cancel)
            .icon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .content(SpannedString(context.getString(content)))
            .autoDismiss(true)
            .onAny(singleButtonCallback)
            .show()
    }

    @JvmStatic
    fun createMessage(context: Context, title: String, content: String) {
        createDefaultDialog(context)
            .title(title)
            .positiveText(R.string.Close)
            .icon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .content(MarkDownUtil.convert(content))
            .autoDismiss(true)
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
        singleButtonCallback: MaterialDialog.SingleButtonCallback
    ) {
        createDefaultDialog(context)
            .title(title)
            .positiveText(positive)
            .negativeText(negative)
            .neutralText(neutral)
            .icon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .content(MarkDownUtil.convert(content))
            .autoDismiss(true)
            .onAny(singleButtonCallback)
            .show()
    }

    @JvmStatic
    fun createMessage(
        context: Context,
        title: String,
        content: String,
        @StringRes positive: Int,
        @StringRes negative: Int,
        singleButtonCallback: MaterialDialog.SingleButtonCallback
    ) {
        createDefaultDialog(context)
            .title(title)
            .positiveText(positive)
            .negativeText(negative)
            .icon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .content(MarkDownUtil.convert(content))
            .autoDismiss(true)
            .onAny(singleButtonCallback)
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
        singleButtonCallback: MaterialDialog.SingleButtonCallback
    ) {
        val builder = createDefaultDialog(context)
            .title(title)
            .positiveText(positive)
            .negativeText(negative)
            .icon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .content(MarkDownUtil.convert(content))
            .autoDismiss(true)
            .onAny(singleButtonCallback)

        if (isSpoiler == true)
            builder.icon(requireNotNull(context.getCompatDrawable(R.drawable.ic_spoiler_tag)))
        else
            builder.icon(requireNotNull(context.getCompatDrawable(R.drawable.ic_loyalty_white_24dp)))

        builder.show()
    }

    @JvmStatic
    fun <T> createCheckList(
        context: Context,
        @StringRes title: Int,
        selectableItems: Collection<T>,
        selectedIndices: Array<Int>,
        listCallbackMultiChoice: MaterialDialog.ListCallbackMultiChoice,
        singleButtonCallback: MaterialDialog.SingleButtonCallback
    ) {
        createDefaultDialog(context)
            .title(title)
            .items(selectableItems)
            .positiveText(R.string.Ok)
            .negativeText(R.string.Reset)
            .neutralText(R.string.Cancel)
            .icon(requireNotNull(context.getCompatTintedDrawable(R.drawable.ic_new_releases_white_24dp)))
            .itemsCallbackMultiChoice(selectedIndices, listCallbackMultiChoice)
            .autoDismiss(true)
            .onAny(singleButtonCallback)
            .show()
    }

    @JvmStatic
    fun createChangeLog(context: Context) {
        try {
            val materialDialog = createDefaultDialog(context)
                .customView(R.layout.dialog_changelog, true)
                .build()

            val singleLineTextView =
                materialDialog.findViewById(R.id.changelog_version) as? SingleLineTextView
            singleLineTextView?.setText(String.format("v%s", BuildConfig.versionName))

            val inputStream = context.assets.open("changelog.md")
            val stringBuilder = StringBuilder()
            var buffer = inputStream.read()
            while (buffer != -1) {
                stringBuilder.append(buffer.toChar())
                buffer = inputStream.read()
            }
            val richMarkdownTextView =
                materialDialog.findViewById(R.id.changelog_information) as? RichMarkdownTextView
            richMarkdownTextView?.richMarkDown(stringBuilder.toString())

            materialDialog.show()
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    /**
     * Builds themed material dialog builder for basic configuration
     *
     * @param context from a fragment activity derived class
     * @see FragmentActivity
     */
    @JvmStatic
    fun createDefaultDialog(context: Context): MaterialDialog.Builder =
        MaterialDialog.Builder(context)
            .typeface(Typeface.SANS_SERIF, Typeface.SANS_SERIF)
            .buttonRippleColorRes(R.color.colorAccentDark)
            .positiveColorRes(R.color.colorStateGreen)
            .negativeColorRes(R.color.colorStateOrange)
            .neutralColorRes(R.color.colorStateBlue)
            .theme(
                if (CompatUtil.isLightTheme(KoinExt.get(Settings::class.java)))
                    Theme.LIGHT
                else
                    Theme.DARK
            )
}
