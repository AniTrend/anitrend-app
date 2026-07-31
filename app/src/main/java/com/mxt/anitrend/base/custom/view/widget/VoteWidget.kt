package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetVoteBinding
import com.mxt.anitrend.extension.getCompatColor
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber
import java.util.Locale

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
    View.OnClickListener {

    interface Listener {
        fun onRateReview(
            id: Long,
            rating: ReviewRating?,
        )
    }

    private lateinit var binding: WidgetVoteBinding
    private var model: Review? = null
    private val tagName = VoteWidget::class.java.simpleName
    private var listener: Listener? = null
    private var recycled = false
    private var currentUser: UserBase? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setCurrentUser(user: UserBase?) {
        this.currentUser = user
    }

    @ColorRes
    private var colorStyle: Int = 0

    init {
        onInit()
    }

    private fun performRating(ratingType: String) {
        val currentModel = model ?: return
        val rating = try {
            ReviewRating.valueOf(ratingType)
        } catch (e: Exception) {
            Timber.e(e, "Invalid rating type: $ratingType")
            resetFlipperState()
            return
        }
        listener?.onRateReview(currentModel.id, rating)
        if (!recycled && isAttachedToWindow) {
            resetFlipperState()
        }
    }

    override fun onClick(view: View) {
        if (currentUser != null) {
            when (view.id) {
                R.id.widget_thumb_up_flipper -> {
                    if (binding.widgetThumbUpFlipper.displayedChild == CONTENT_STATE) {
                        binding.widgetThumbUpFlipper.showNext()
                        val current = model?.userRating
                        val rating = if (current == KeyUtil.UP_VOTE) KeyUtil.NO_VOTE else KeyUtil.UP_VOTE
                        performRating(rating)
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
                    if (binding.widgetThumbDownFlipper.displayedChild == CONTENT_STATE) {
                        binding.widgetThumbDownFlipper.showNext()
                        val current = model?.userRating
                        val rating = if (current == KeyUtil.DOWN_VOTE) KeyUtil.NO_VOTE else KeyUtil.DOWN_VOTE
                        performRating(rating)
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
        binding = WidgetVoteBinding.inflate(LayoutInflater.from(context), this, true)
        binding.widgetThumbUpFlipper.setOnClickListener(this)
        binding.widgetThumbDownFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        recycled = true
        listener = null
        resetFlipperState()
        model = null
    }

    private fun resetFlipperState() {
        if (binding.widgetThumbUpFlipper.displayedChild == LOADING_STATE) {
            binding.widgetThumbUpFlipper.displayedChild = CONTENT_STATE
        }

        if (binding.widgetThumbDownFlipper.displayedChild == LOADING_STATE) {
            binding.widgetThumbDownFlipper.displayedChild = CONTENT_STATE
        }
    }

    fun setModel(
        model: Review,
        @ColorRes colorStyle: Int,
    ) {
        recycled = false
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
                        R.color.colorStateGreen,
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
                        R.color.colorStateOrange,
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

        binding.widgetThumbUp.text = convertToText(currentModel.rating)
        val downVotes = currentModel.ratingAmount - currentModel.rating
        binding.widgetThumbDown.text = convertToText(if (downVotes < 0) 0 else downVotes)
        resetFlipperState()
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1

        fun convertToText(count: Int): String = String.format(Locale.getDefault(), " %d ", count)
    }
}
