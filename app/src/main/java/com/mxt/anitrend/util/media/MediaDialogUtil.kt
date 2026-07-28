package com.mxt.anitrend.util.media

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.view.sheet.BottomSheetSeriesManage
import timber.log.Timber

/**
 * Utility for showing the M3 media list management bottom sheet.
 */
internal object MediaDialogUtil {
    private val tagName = MediaDialogUtil::class.java.simpleName
    private const val TAG_MANAGE_SHEET = "media_manage_sheet"

    /**
     * Shows the BottomSheetSeriesManage dialog for the given media entry.
     *
     * @param context must be a FragmentActivity derivative
     * @param mediaBase non-null series model object off or on the user's list
     */
    @JvmStatic
    fun createSeriesManage(
        context: Context,
        mediaBase: MediaBase,
    ) {
        val activity = context as? FragmentActivity ?: run {
            Timber.tag(tagName).e("Cannot show BottomSheetSeriesManage: context is not a FragmentActivity")
            return
        }
        val sheet = BottomSheetSeriesManage.newInstance(mediaBase)
        sheet.show(activity.supportFragmentManager, TAG_MANAGE_SHEET)
    }
}
