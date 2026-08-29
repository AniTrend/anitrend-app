package com.mxt.anitrend.view.sheet

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.widget.CustomRatingBar
import com.mxt.anitrend.binding.markDown
import com.mxt.anitrend.binding.richMarkDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.BottomSheetReviewBinding
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.extension.parcelable
import com.mxt.anitrend.navigation.extension.screenParam
import com.mxt.anitrend.navigation.extension.screenParamKey
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/11/05.
 * Review reader bottom sheet
 *
 * Renders the immutable [ReviewRecord] supplied by the hosting screen. Navigation is
 * identity-only: the bundle carries [ReviewScreenParam] (review, media, and user ids)
 * instead of a complete review entity, and rendering happens from the canonical record
 * already held by the screen that opened the sheet.
 */
class BottomReviewReader : BottomSheetBase<ReviewRecord>() {
    private var model: ReviewRecord? = null
    private var reviewParam: ReviewScreenParam? = null
    private var binding: BottomSheetReviewBinding? = null
    private var onUserClick: ((UserScreenParam) -> Unit)? = null

    /** Factory and argument helpers for the review reader sheet. */
    companion object {
        /** Creates a review reader sheet with the supplied arguments. */
        @JvmStatic
        fun newInstance(bundle: Bundle): BottomReviewReader = BottomReviewReader().apply {
            arguments = bundle
        }

        /**
         * Resolves the review identity from the sheet arguments.
         *
         * The typed [ReviewScreenParam] under the stable ARG_REVIEW_SCREEN key wins
         * when present; otherwise the legacy arg_model channel (pre-migration
         * builders) is bridged.
         */
        fun fromBundle(bundle: Bundle?): ReviewScreenParam? = resolve(
            typed = bundle?.screenParam<ReviewScreenParam>(),
            legacy = bundle?.parcelable(KeyUtil.arg_model),
        )

        @VisibleForTesting
        internal fun resolve(typed: ReviewScreenParam?, legacy: ReviewScreenParam?): ReviewScreenParam? {
            typed?.let { return it }
            return legacy
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewParam = fromBundle(arguments)
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
        review.user?.let { user ->
            reviewTemplate.userAvatar.setImage(user.avatar)
            reviewTemplate.userName.text = user.name
        }
        reviewTemplate.reviewDate.text = DateUtil.convertDate(review.createdAt)
        reviewTemplate.seriesTitle.setTitle(review)
        reviewTemplate.reviewSummary.markDown(review.summary)
        CustomRatingBar.setAverageScore(reviewTemplate.reviewScore, review.score)
        AspectImageView.setImage(reviewTemplate.reviewCover, review.media?.coverImage)
        binding?.reviewBody?.richMarkDown(review.body)
        reviewTemplate.userAvatar.setOnClickListener {
            val userId = review.user?.id ?: reviewParam?.userId ?: return@setOnClickListener
            onUserClick?.invoke(UserScreenParam(userId))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        onUserClick = null
    }

    /** Builds a review reader sheet from an immutable review record. */
    class Builder : BottomSheetBuilder() {
        private var review: ReviewRecord? = null
        private var onUserClick: ((UserScreenParam) -> Unit)? = null

        override fun build(): BottomSheetBase<*> = newInstance(bundle).also { instance ->
            instance.model = this@Builder.review
            instance.onUserClick = onUserClick
        }

        /** Sets the callback invoked when the review author is selected. */
        fun setOnUserClick(listener: (UserScreenParam) -> Unit): Builder {
            onUserClick = listener
            return this
        }

        /** Sets the review rendered by the sheet and its identity-only arguments. */
        fun setReview(review: ReviewRecord): Builder {
            this.review = review
            val param = review.toReviewScreenParam()
            // Stable-key write at the production entry point; the legacy arg_model
            // channel is retained for pre-migration readers.
            bundle.putParcelable(screenParamKey<ReviewScreenParam>(), param)
            bundle.putParcelable(KeyUtil.arg_model, param)
            return this
        }
    }
}

/**
 * Production writer mapping for the review reader sheet: extracts only the stable
 * review, media, and user identities from a [ReviewRecord]. The entity itself is
 * never parceled into the sheet bundle.
 */
@VisibleForTesting
internal fun ReviewRecord.toReviewScreenParam(): ReviewScreenParam = ReviewScreenParam(
    reviewId = id,
    mediaId = media?.id,
    mediaType = media?.type,
    userId = user?.id,
)
