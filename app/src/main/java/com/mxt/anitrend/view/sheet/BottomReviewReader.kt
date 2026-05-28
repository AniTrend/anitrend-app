package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.widget.CustomRatingBar
import com.mxt.anitrend.binding.markDown
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.BottomSheetReviewBinding
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil
import com.mxt.anitrend.view.activity.detail.ProfileActivity

/**
 * Created by max on 2017/11/05.
 * Review reader bottom sheet
 */
class BottomReviewReader : BottomSheetBase<Review>() {

    private var model: Review? = null
    private var binding: BottomSheetReviewBinding? = null

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomReviewReader {
            return BottomReviewReader().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = arguments?.parcelable(KeyUtil.arg_model)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        binding = BottomSheetReviewBinding.inflate(layoutInflater)
        dialog.setContentView(requireNotNull(binding).root)
        bindToolbarViews(requireNotNull(binding).root)
        createBottomSheetBehavior(requireNotNull(binding).root)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val review = model ?: return
        val reviewTemplate = binding?.reviewTemplate ?: return
        reviewTemplate.userAvatar.setImage(review.user?.avatar)
        reviewTemplate.userName.text = review.user?.name
        reviewTemplate.reviewDate.text = DateUtil.convertDate(review.createdAt)
        reviewTemplate.seriesTitle.setTitle(review)
        reviewTemplate.reviewSummary.markDown(review.summary)
        CustomRatingBar.setAverageScore(reviewTemplate.reviewScore, review.score)
        AspectImageView.setImage(reviewTemplate.reviewCover, review.media?.coverImage)
        binding?.reviewBody?.richMarkDown(review.body)
        reviewTemplate.userAvatar.setOnClickListener { view ->
            val host = activity ?: return@setOnClickListener
            val userId = review.user?.id ?: return@setOnClickListener
            val intent = Intent(host, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(KeyUtil.arg_id, userId)
            }
            CompatUtil.startRevealAnim(host, view, intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    class Builder : BottomSheetBuilder() {
        override fun build(): BottomSheetBase<*> {
            return newInstance(bundle)
        }

        fun setReview(review: Review): Builder {
            bundle.putParcelable(KeyUtil.arg_model, review)
            return this
        }
    }
}
