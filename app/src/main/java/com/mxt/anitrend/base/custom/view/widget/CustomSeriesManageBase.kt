package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.AdapterView
import android.widget.RelativeLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.spinner.IconArrayAdapter
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.media.MediaListUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by max on 2018/01/20.
 * CustomSeriesManageBase for managing mediaLists
 */
abstract class CustomSeriesManageBase
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RelativeLayout(context, attrs, defStyleAttr),
    CustomView,
    AdapterView.OnItemSelectedListener,
    KoinComponent {

    protected val presenter by inject<BasePresenter>()
    protected lateinit var mediaListModel: MediaList

    protected val indexIconMap: MutableMap<Int, Int> =
        hashMapOf(
            0 to R.drawable.ic_remove_red_eye_white_18dp,
            1 to R.drawable.ic_bookmark_white_24dp,
            2 to R.drawable.ic_done_all_grey_600_24dp,
            3 to R.drawable.ic_delete_red_600_18dp,
            4 to R.drawable.ic_pause_white_18dp,
            5 to R.drawable.ic_repeat_white_18dp,
        )
    protected val mediaListStatuses =
        arrayOf(
            KeyUtil.CURRENT,
            KeyUtil.PLANNING,
            KeyUtil.COMPLETED,
            KeyUtil.DROPPED,
            KeyUtil.PAUSED,
            KeyUtil.REPEATING,
        )

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        // no-op
    }

    protected fun getIconArrayAdapter(): IconArrayAdapter {
        val iconArrayAdapter =
            IconArrayAdapter(
                context,
                R.layout.adapter_spinner_item,
                R.id.spinner_text,
                CompatUtil.getStringList(context, R.array.media_list_status),
            )
        iconArrayAdapter.setIndexIconMap(indexIconMap)
        return iconArrayAdapter
    }

    fun setModel(mediaBase: MediaBase) {
        val entry = mediaBase.mediaListEntry
        mediaListModel =
            if (entry != null) {
                entry.apply { media = mediaBase }
            } else {
                MediaList().apply {
                    mediaId = mediaBase.id
                    media = mediaBase
                }
            }
        bindFields()
        populateFields()
    }

    fun getMediaListOptions(): MediaListOptions = requireNotNull(presenter.database.currentUser).mediaListOptions

    fun getModel(): MediaList = mediaListModel

    /**
     * Saves the current views states into the model
     * and returns a bundle of the params
     * @see MediaListUtil
     */
    abstract fun persistChanges(): Bundle

    protected abstract fun populateFields()

    protected abstract fun bindFields()

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        presenter.onDestroy()
    }

    protected fun getSeriesModel(): MediaBase = mediaListModel.media
}
