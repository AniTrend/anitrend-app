package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.CustomActionMangaBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaListUtil

/**
 * Created by max on 2018/01/03.
 */
class CustomSeriesMangaManage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CustomSeriesManageBase(context, attrs, defStyleAttr) {

    private lateinit var binding: CustomActionMangaBinding

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : this(context, attrs, defStyleAttr)

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        super.onInit()
        binding = CustomActionMangaBinding.inflate(context.getLayoutInflater(), this, true)
    }

    /**
     * Saves the current views states into the model
     * @see MediaListUtil
     */
    override fun persistChanges(): Bundle {
        mediaListModel.progress = binding.diaCurrentChapters.progressCurrent
        mediaListModel.repeat = binding.diaCurrentReread.progressCurrent
        mediaListModel.progressVolumes = binding.diaCurrentVolumes.progressCurrent

        mediaListModel.score = binding.diaCurrentScore.scoreCurrent

        mediaListModel.startedAt = binding.diaCurrentStartedAt.date
        mediaListModel.completedAt = binding.diaCurrentCompletedAt.date

        mediaListModel.isHidden = binding.diaCurrentPrivacy.isChecked
        mediaListModel.notes = binding.diaCurrentNotes.formattedText
        mediaListModel.status = mediaListStatuses[binding.diaCurrentStatus.selectedItemPosition]
        return MediaListUtil.getMediaListParams(mediaListModel, getMediaListOptions().scoreFormat)
    }

    override fun populateFields() {
        binding.diaCurrentNotes.setText(mediaListModel.notes)
    }

    override fun bindFields() {
        // Apply the adapter to the spinner
        binding.diaCurrentStatus.adapter = getIconArrayAdapter()

        val statusList = mediaListStatuses.toList()
        if (!TextUtils.isEmpty(mediaListModel.status))
            binding.diaCurrentStatus.setSelection(statusList.indexOf(mediaListModel.status))
        else
            binding.diaCurrentStatus.setSelection(statusList.indexOf(KeyUtil.PLANNING))

        binding.diaCurrentPrivacy.isChecked = mediaListModel.isHidden

        if (mediaListModel.media.volumes > 0)
            binding.diaCurrentVolumes.setProgressMaximum(mediaListModel.media.volumes)
        if (mediaListModel.media.chapters > 0)
            binding.diaCurrentChapters.setProgressMaximum(mediaListModel.media.chapters)

        binding.diaCurrentScore.setScoreFormat(getMediaListOptions().scoreFormat)
        binding.diaCurrentScore.setScoreCurrent(mediaListModel.score)

        binding.diaCurrentChapters.setProgressCurrent(mediaListModel.progress)
        binding.diaCurrentVolumes.setProgressCurrent(mediaListModel.progressVolumes)
        binding.diaCurrentReread.setProgressCurrent(mediaListModel.repeat)
        binding.diaCurrentStartedAt.setDate(mediaListModel.startedAt)
        binding.diaCurrentCompletedAt.setDate(mediaListModel.completedAt)

        binding.diaCurrentStatus.onItemSelectedListener = this
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        super.onViewRecycled()
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        mediaListModel.status = mediaListStatuses[i]
        when (mediaListStatuses[i]) {
            KeyUtil.CURRENT -> {
                if (CompatUtil.equals(getSeriesModel().status, KeyUtil.NOT_YET_RELEASED))
                    NotifyUtil.makeText(context, R.string.warning_manga_not_publishing, Toast.LENGTH_LONG).show()
            }
            KeyUtil.PLANNING -> Unit
            KeyUtil.COMPLETED -> {
                if (!CompatUtil.equals(getSeriesModel().status, KeyUtil.FINISHED))
                    NotifyUtil.makeText(context, R.string.warning_manga_publishing, Toast.LENGTH_LONG).show()
                else {
                    var total = getSeriesModel().chapters
                    mediaListModel.progress = total
                    binding.diaCurrentChapters.setProgressCurrent(total)
                    total = getSeriesModel().volumes
                    if (total > 0) {
                        mediaListModel.progressVolumes = total
                        binding.diaCurrentVolumes.setProgressCurrent(total)
                    }
                }
            }
            else -> {
                if (CompatUtil.equals(getSeriesModel().status, KeyUtil.NOT_YET_RELEASED))
                    NotifyUtil.makeText(context, R.string.warning_manga_not_publishing, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) = Unit
}
