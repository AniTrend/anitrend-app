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
import com.mxt.anitrend.databinding.CustomActionAnimeBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.media.MediaListUtil

/**
 * Created by max on 2018/01/03.
 */
class CustomSeriesAnimeManage
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CustomSeriesManageBase(context, attrs, defStyleAttr) {
    private lateinit var binding: CustomActionAnimeBinding

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        super.onInit()
        binding = CustomActionAnimeBinding.inflate(context.getLayoutInflater(), this, true)
    }

    /**
     * Saves the current views states into the model
     *
     * @see MediaListUtil
     */
    override fun persistChanges(): Bundle {
        mediaListModel.progress = binding.diaCurrentProgress.progressCurrent
        mediaListModel.repeat = binding.diaCurrentRewatch.progressCurrent

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
        if (!TextUtils.isEmpty(mediaListModel.status)) {
            binding.diaCurrentStatus.setSelection(statusList.indexOf(mediaListModel.status))
        } else {
            binding.diaCurrentStatus.setSelection(statusList.indexOf(KeyUtil.PLANNING))
        }

        binding.diaCurrentPrivacy.isChecked = mediaListModel.isHidden
        if (mediaListModel.media.episodes > 0) {
            binding.diaCurrentProgress.setProgressMaximum(mediaListModel.media.episodes)
        }

        binding.diaCurrentScore.setScoreFormat(getMediaListOptions().scoreFormat)
        binding.diaCurrentScore.setScoreCurrent(mediaListModel.score)

        binding.diaCurrentProgress.setProgressCurrent(mediaListModel.progress)
        binding.diaCurrentRewatch.setProgressCurrent(mediaListModel.repeat)
        binding.diaCurrentStartedAt.setDate(mediaListModel.startedAt)
        binding.diaCurrentCompletedAt.setDate(mediaListModel.completedAt)

        binding.diaCurrentStatus.onItemSelectedListener = this
    }

    override fun onItemSelected(
        adapterView: AdapterView<*>,
        view: View,
        i: Int,
        l: Long,
    ) {
        mediaListModel.status = mediaListStatuses[i]
        when (mediaListStatuses[i]) {
            KeyUtil.CURRENT -> {
                if (CompatUtil.equals(getSeriesModel().status, KeyUtil.NOT_YET_RELEASED)) {
                    NotifyUtil.makeText(context, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show()
                }
            }
            KeyUtil.PLANNING -> Unit
            KeyUtil.COMPLETED -> {
                if (!CompatUtil.equals(getSeriesModel().status, KeyUtil.FINISHED)) {
                    NotifyUtil.makeText(context, R.string.warning_anime_is_airing, Toast.LENGTH_LONG).show()
                } else {
                    val total = getSeriesModel().episodes
                    mediaListModel.progress = total
                    binding.diaCurrentProgress.setProgressCurrent(total)
                }
            }
            else -> {
                if (CompatUtil.equals(getSeriesModel().status, KeyUtil.NOT_YET_RELEASED)) {
                    NotifyUtil.makeText(context, R.string.warning_anime_not_airing, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) = Unit
}
