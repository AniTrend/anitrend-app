package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.base.custom.view.widget.CustomRatingBar
import com.mxt.anitrend.base.custom.view.widget.VoteWidget
import com.mxt.anitrend.binding.markDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.databinding.AdapterReviewBinding
import com.mxt.anitrend.databinding.AdapterSeriesReviewBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.anilist.Review
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/10/30.
 * Media review adapter
 */
class ReviewAdapter(
    context: Context,
    private val coordinator: WidgetMutationCoordinator,
    private val isMediaType: Boolean = false,
) : RecyclerViewAdapter<Review>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<Review> = if (!isMediaType) {
        ReviewBanner(
            AdapterReviewBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    } else {
        ReviewDefault(
            AdapterSeriesReviewBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    }

    override fun getFilter(): Filter? = null

    private val voteListener = object : VoteWidget.Listener {
        override fun onRateReview(
            id: Long,
            rating: ReviewRating?,
            onResult: (Result<Review>) -> Unit,
        ) = coordinator.rateReview(id, rating, onResult)
    }

    inner class ReviewBanner(
        private val binding: AdapterReviewBinding,
    ) : RecyclerViewHolder<Review>(binding.root) {
        init {
            bindClickListeners(R.id.series_image, R.id.review_read_more)
            bindLongClickListeners(R.id.series_image)
        }

        override fun onBindViewHolder(model: Review) {
            WideImageView.setImage(binding.seriesImage, model.media.bannerImage)
            binding.reviewUserName.text = model.user.name
            CustomRatingBar.setAverageScore(binding.seriesRating, model.score)
            binding.seriesTitle.setTitle(model)
            binding.reviewVote.setModel(model, R.color.white)
            binding.reviewVote.setCurrentUser(coordinator.databaseHelper.currentUser)
            binding.reviewVote.setListener(voteListener)
            binding.reviewSummary.text = model.summary
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.seriesImage)
            binding.reviewVote.setListener(null)
            binding.reviewVote.onViewRecycled()
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }

    inner class ReviewDefault(
        private val binding: AdapterSeriesReviewBinding,
    ) : RecyclerViewHolder<Review>(binding.root) {
        init {
            bindClickListeners(R.id.review_read_more, R.id.user_avatar)
            bindLongClickListeners(R.id.series_image)
        }

        override fun onBindViewHolder(model: Review) {
            binding.userAvatar.setImage(model.user.avatar)
            binding.userName.text = model.user.name
            binding.reviewDate.text = DateUtil.convertDate(model.createdAt)
            binding.seriesTitle.setTitle(model)
            binding.reviewSummary.markDown(model.summary)
            CustomRatingBar.setAverageScore(binding.seriesRating, model.score)
            binding.reviewVote.setModel(model, 0)
            binding.reviewVote.setCurrentUser(coordinator.databaseHelper.currentUser)
            binding.reviewVote.setListener(voteListener)
            AspectImageView.setImage(binding.seriesImage, model.media.coverImage)
        }

        override fun onViewRecycled() {
            Glide.with(getContext()).clear(binding.seriesImage)
            binding.reviewVote.setListener(null)
            binding.reviewVote.onViewRecycled()
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
