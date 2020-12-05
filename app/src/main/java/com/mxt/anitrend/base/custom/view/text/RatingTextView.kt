package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.CustomRatingWidgetBinding
import com.mxt.anitrend.extension.LAZY_MODE_UNSAFE
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.MediaListOptions
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Created by max on 2018/01/27.
 * Special text base rating view
 */

class RatingTextView : LinearLayout, CustomView, KoinComponent {

    private var mediaListOptions: MediaListOptions? = null
    private val presenter by inject<BasePresenter>()
    private val binding by lazy(LAZY_MODE_UNSAFE) {
        CustomRatingWidgetBinding.inflate(getLayoutInflater(), this, true)
    }

    constructor(context: Context) : super(context) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        val basePresenter = BasePresenter(context)
        if (presenter.settings.isAuthenticated)
            mediaListOptions = basePresenter.database.currentUser?.mediaListOptions
    }

    fun setFavourState(isFavourite: Boolean) {
        @ColorRes val colorTint = if (isFavourite) R.color.colorStateYellow else R.color.white
        val drawable = context.getCompatDrawable(R.drawable.ic_star_grey_600_24dp, colorTint)
        binding.ratingFavourState.setImageDrawable(drawable)
    }

    fun setListStatus(mediaBase: MediaBase) {
        if (mediaBase.mediaListEntry != null) {
            binding.ratingListStatus.visibility = View.VISIBLE
            when (mediaBase.mediaListEntry?.status) {
                KeyUtil.CURRENT -> binding.ratingListStatus.setTintDrawable(R.drawable.ic_remove_red_eye_white_18dp,
                        R.color.white)
                KeyUtil.PLANNING -> binding.ratingListStatus.setTintDrawable(R.drawable.ic_bookmark_white_24dp,
                        R.color.white)
                KeyUtil.COMPLETED -> binding.ratingListStatus.setTintDrawable(R.drawable.ic_done_all_grey_600_24dp,
                        R.color.white)
                KeyUtil.DROPPED -> binding.ratingListStatus.setTintDrawable(R.drawable.ic_delete_red_600_18dp,
                        R.color.white)
                KeyUtil.PAUSED -> binding.ratingListStatus.setTintDrawable(R.drawable.ic_pause_white_18dp,
                        R.color.white)
                KeyUtil.REPEATING -> binding.ratingListStatus.setTintDrawable(R.drawable.ic_repeat_white_18dp,
                        R.color.white)
            }
        } else
            binding.ratingListStatus.visibility = View.GONE
    }

    fun setListStatus() {
        binding.ratingListStatus.visibility = View.GONE
    }

    fun setRating(mediaList: MediaList) {
        if (mediaListOptions != null)
            when (mediaListOptions?.scoreFormat) {
                KeyUtil.POINT_10_DECIMAL -> binding.ratingValue.text = String.format(Locale.getDefault(), "%.1f", mediaList.score)
                KeyUtil.POINT_100, KeyUtil.POINT_10, KeyUtil.POINT_5 -> binding.ratingValue.text = String.format(Locale.getDefault(), "%d", mediaList.score.toInt())
                KeyUtil.POINT_3 -> {
                    binding.ratingValue.text = ""
                    when (mediaList.score.toInt()) {
                        0 -> binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_face_white_18dp), null, null, null)
                        1 -> binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_sentiment_dissatisfied_white_18dp), null, null, null)
                        2 -> binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_sentiment_neutral_white_18dp), null, null, null)
                        3 -> binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_sentiment_satisfied_white_18dp), null, null, null)
                    }
                }
            }
        else
            binding.ratingValue.text = String.format(Locale.getDefault(), "%d", mediaList.score.toInt())
    }

    fun setRating(mediaBase: MediaBase) {
        var mediaScoreDefault = mediaBase.averageScore.toFloat() * 5 / 100f
        if (mediaListOptions != null)
            when (mediaListOptions?.scoreFormat) {
                KeyUtil.POINT_10_DECIMAL -> {
                    mediaScoreDefault = mediaBase.averageScore / 10f
                    binding.ratingValue.text = String.format(Locale.getDefault(), "%.1f", mediaScoreDefault)
                }
                KeyUtil.POINT_100 -> binding.ratingValue.text = String.format(Locale.getDefault(), "%d", mediaBase.averageScore)
                KeyUtil.POINT_10 -> {
                    mediaScoreDefault = mediaBase.averageScore / 10f
                    binding.ratingValue.text = String.format(Locale.getDefault(), "%d", mediaScoreDefault.toInt())
                }
                KeyUtil.POINT_5 -> binding.ratingValue.text = String.format(Locale.getDefault(), "%d", mediaScoreDefault.toInt())
                KeyUtil.POINT_3 -> {
                    binding.ratingValue.text = ""
                    if (mediaBase.averageScore == 0)
                        binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_face_white_18dp), null, null, null)
                    when {
                        mediaBase.averageScore in 1..33 ->
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_sentiment_dissatisfied_white_18dp), null, null, null)
                        mediaBase.averageScore in 34..66 ->
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_sentiment_neutral_white_18dp), null, null, null)
                        mediaBase.averageScore in 67..100 ->
                            binding.ratingValue.setCompoundDrawablesWithIntrinsicBounds(context.getCompatDrawable(R.drawable.ic_sentiment_satisfied_white_18dp), null, null, null)
                    }
                }
            }
        else
            binding.ratingValue.text = String.format(Locale.getDefault(), "%d", mediaBase.averageScore)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {

    }
}
