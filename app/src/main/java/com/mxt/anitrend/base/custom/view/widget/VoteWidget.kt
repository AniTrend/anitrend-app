package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetVoteBinding
import com.mxt.anitrend.extension.getCompatColor
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.apiError
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

/**
 * Created by max on 2017/11/05.
 * Up Vote and Down Vote views
 */
class VoteWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener,
    RetroCallback<Review> {
    private lateinit var presenter: WidgetPresenter<Review>
    private lateinit var binding: WidgetVoteBinding
    private var model: Review? = null
    private val tagName = VoteWidget::class.java.simpleName

    @ColorRes
    private var colorStyle: Int = 0

    init {
        onInit()
    }

    private fun setParameters(
        @KeyUtil.ReviewRating ratingType: String,
    ) {
        val currentModel = model ?: return
        presenter.params.apply {
            putLong(KeyUtil.arg_id, currentModel.id)
            putString(KeyUtil.arg_rating, ratingType)
            putBoolean(KeyUtil.arg_asHtml, false)
        }
        presenter.requestData(KeyUtil.MUT_RATE_REVIEW, context, this)
    }

    override fun onClick(view: View) {
        if (presenter.settings.isAuthenticated) {
            when (view.id) {
                R.id.widget_thumb_up_flipper -> {
                    if (binding.widgetThumbUpFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetThumbUpFlipper.showNext()
                        val current = model?.userRating
                        val rating = if (current == KeyUtil.UP_VOTE) KeyUtil.NO_VOTE else KeyUtil.UP_VOTE
                        setParameters(rating)
                    } else {
                        NotifyUtil
                            .makeText(
                                context,
                                R.string.busy_please_wait,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
                R.id.widget_thumb_down_flipper -> {
                    if (binding.widgetThumbDownFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                        binding.widgetThumbDownFlipper.showNext()
                        val current = model?.userRating
                        val rating = if (current == KeyUtil.DOWN_VOTE) KeyUtil.NO_VOTE else KeyUtil.DOWN_VOTE
                        setParameters(rating)
                    } else {
                        NotifyUtil
                            .makeText(
                                context,
                                R.string.busy_please_wait,
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
            }
        } else {
            NotifyUtil
                .makeText(
                    context,
                    R.string.info_login_req,
                    R.drawable.ic_group_add_grey_600_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        presenter = WidgetPresenter(context)
        binding = WidgetVoteBinding.inflate(LayoutInflater.from(context), this, true)
        binding.widgetThumbUpFlipper.setOnClickListener(this)
        binding.widgetThumbDownFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        presenter.onDestroy()
        model = null
    }

    private fun resetFlipperState() {
        if (binding.widgetThumbUpFlipper.displayedChild == WidgetPresenter.LOADING_STATE) {
            binding.widgetThumbUpFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
        }

        if (binding.widgetThumbDownFlipper.displayedChild == WidgetPresenter.LOADING_STATE) {
            binding.widgetThumbDownFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
        }
    }

    fun setModel(
        model: Review,
        @ColorRes colorStyle: Int,
    ) {
        this.model = model
        this.colorStyle = colorStyle
        resetFlipperState()
        setReviewStatus()
    }

    private fun applyColorStyleTo(
        singleLineTextView: SingleLineTextView,
        @DrawableRes drawableItem: Int,
    ) {
        if (colorStyle != 0) {
            singleLineTextView.setCompoundDrawablesWithIntrinsicBounds(
                context.getCompatDrawable(drawableItem, colorStyle),
                null,
                null,
                null,
            )
            singleLineTextView.setTextColor(context.getCompatColor(colorStyle))
        } else {
            singleLineTextView.setCompoundDrawablesWithIntrinsicBounds(
                context.getCompatDrawable(drawableItem, R.color.colorGrey600),
                null,
                null,
                null,
            )
        }
    }

    private fun setReviewStatus() {
        val currentModel = model ?: return
        if (colorStyle != 0) {
            binding.widgetThumbUp.setTextColor(context.getCompatColor(colorStyle))
            binding.widgetThumbDown.setTextColor(context.getCompatColor(colorStyle))
        }
        when (currentModel.userRating) {
            KeyUtil.UP_VOTE -> {
                binding.widgetThumbUp.setCompoundDrawablesWithIntrinsicBounds(
                    context.getCompatDrawable(
                        R.drawable.ic_thumb_up_grey_600_18dp,
                        R.color.colorStateGreen
                    ),
                    null,
                    null,
                    null,
                )
                applyColorStyleTo(binding.widgetThumbDown, R.drawable.ic_thumb_down_grey_600_18dp)
            }
            KeyUtil.DOWN_VOTE -> {
                binding.widgetThumbDown.setCompoundDrawablesWithIntrinsicBounds(
                    context.getCompatDrawable(
                        R.drawable.ic_thumb_down_grey_600_18dp,
                        R.color.colorStateOrange
                    ),
                    null,
                    null,
                    null,
                )
                applyColorStyleTo(binding.widgetThumbUp, R.drawable.ic_thumb_up_grey_600_18dp)
            }
            else -> {
                applyColorStyleTo(binding.widgetThumbUp, R.drawable.ic_thumb_up_grey_600_18dp)
                applyColorStyleTo(binding.widgetThumbDown, R.drawable.ic_thumb_down_grey_600_18dp)
            }
        }

        binding.widgetThumbUp.text = WidgetPresenter.convertToText(currentModel.rating)
        val downVotes = currentModel.ratingAmount - currentModel.rating
        binding.widgetThumbDown.text = WidgetPresenter.convertToText(if (downVotes < 0) 0 else downVotes)
        resetFlipperState()
    }

    /**
     * Invoked for a received HTTP response.
     */
    override fun onResponse(
        call: Call<Review>,
        response: Response<Review>,
    ) {
        try {
            val responseModel = response.body()
            if (response.isSuccessful && responseModel != null) {
                model?.apply {
                    rating = responseModel.rating
                    ratingAmount = responseModel.ratingAmount
                    userRating = responseModel.userRating
                }
                setReviewStatus()
            } else {
                Timber.w(response.apiError())
                resetFlipperState()
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    override fun onFailure(
        call: Call<Review>,
        throwable: Throwable,
    ) {
        try {
            Timber.e(throwable)
            resetFlipperState()
        } catch (e: Exception) {
            Timber.e(throwable)
        }
    }
}
