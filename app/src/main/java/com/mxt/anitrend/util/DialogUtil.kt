package com.mxt.anitrend.util

import android.content.Context
import android.content.DialogInterface
import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.util.markdown.MarkDownUtil

/**
 * Created by max on 2017/09/16.
 * Creates different dialog types
 */
object DialogUtil {
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
