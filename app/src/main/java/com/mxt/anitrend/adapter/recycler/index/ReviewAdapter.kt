package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.image.AspectImageView
import com.mxt.anitrend.base.custom.view.image.WideImageView
import com.mxt.anitrend.base.custom.view.widget.CustomRatingBar
import com.mxt.anitrend.base.custom.view.widget.VoteWidget
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.binding.markDown
import com.mxt.anitrend.binding.setImage
import com.mxt.anitrend.databinding.AdapterReviewBinding
import com.mxt.anitrend.databinding.AdapterSeriesReviewBinding
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.date.DateUtil

/**
 * Created by max on 2017/10/30.
 * Media review adapter
 *
 * Immutable [ListAdapter] over [ReviewRecord]: items are submitted whole, never mutated
 * after submission, and diffed on stable review ids. The ViewHolder layouts, view ids,
 * and click callbacks are unchanged from the legacy path.
 */
class ReviewAdapter(
    context: Context,
    private val currentUser: UserBase?,
    private val onRateReviewAction: (Long, ReviewRating?) -> Unit,
    private val isMediaType: Boolean = false,
) : ListAdapter<ReviewRecord, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val appContext = context.applicationContext

    var clickListener: ItemClickListener<ReviewRecord>? = null

    private val voteListener = object : VoteWidget.Listener {
        override fun onRateReview(
            id: Long,
            rating: ReviewRating?,
        ) = onRateReviewAction(id, rating)
    }

    override fun getItemViewType(position: Int): Int = if (!isMediaType) VIEW_TYPE_BANNER else VIEW_TYPE_DEFAULT

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder = if (viewType == VIEW_TYPE_BANNER) {
        ReviewBanner(
            AdapterReviewBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    } else {
        ReviewDefault(
            AdapterSeriesReviewBinding.inflate(parent.context.getLayoutInflater(), parent, false),
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val record = getItem(position)
        when (holder) {
            is ReviewBanner -> holder.bind(record)
            is ReviewDefault -> holder.bind(record)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is ReviewBanner -> holder.recycle()
            is ReviewDefault -> holder.recycle()
        }
    }

    inner class ReviewBanner(
        private val binding: AdapterReviewBinding,
    ) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnLongClickListener {
        init {
            binding.seriesImage.setOnClickListener(this)
            binding.seriesImage.setOnLongClickListener(this)
            binding.reviewReadMore.setOnClickListener(this)
        }

        fun bind(model: ReviewRecord) {
            WideImageView.setImage(binding.seriesImage, model.media?.bannerImage)
            binding.reviewUserName.text = model.user?.name
            CustomRatingBar.setAverageScore(binding.seriesRating, model.score)
            binding.seriesTitle.setTitle(model)
            binding.reviewVote.setModel(model, R.color.white)
            binding.reviewVote.setCurrentUser(currentUser)
            binding.reviewVote.setListener(voteListener)
            binding.reviewSummary.text = model.summary
        }

        fun recycle() {
            Glide.with(appContext).clear(binding.seriesImage)
            binding.reviewVote.setListener(null)
            binding.reviewVote.onViewRecycled()
        }

        override fun onClick(v: View) {
            forwardClick(v, bindingAdapterPosition)
        }

        override fun onLongClick(v: View): Boolean {
            forwardLongClick(v, bindingAdapterPosition)
            return true
        }
    }

    inner class ReviewDefault(
        private val binding: AdapterSeriesReviewBinding,
    ) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        View.OnLongClickListener {
        init {
            binding.reviewReadMore.setOnClickListener(this)
            binding.userAvatar.setOnClickListener(this)
            binding.seriesImage.setOnLongClickListener(this)
        }

        fun bind(model: ReviewRecord) {
            binding.userAvatar.setImage(model.user?.avatar)
            binding.userName.text = model.user?.name
            binding.reviewDate.text = DateUtil.convertDate(model.createdAt)
            binding.seriesTitle.setTitle(model)
            binding.reviewSummary.markDown(model.summary)
            CustomRatingBar.setAverageScore(binding.seriesRating, model.score)
            binding.reviewVote.setModel(model, 0)
            binding.reviewVote.setCurrentUser(currentUser)
            binding.reviewVote.setListener(voteListener)
            AspectImageView.setImage(binding.seriesImage, model.media?.coverImage)
        }

        fun recycle() {
            Glide.with(appContext).clear(binding.seriesImage)
            binding.reviewVote.setListener(null)
            binding.reviewVote.onViewRecycled()
        }

        override fun onClick(v: View) {
            forwardClick(v, bindingAdapterPosition)
        }

        override fun onLongClick(v: View): Boolean {
            forwardLongClick(v, bindingAdapterPosition)
            return true
        }
    }

    private fun forwardClick(
        target: View,
        position: Int,
    ) {
        val listener = clickListener ?: return
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        listener.onItemClick(target, IndexedValue(position, getItem(position)))
    }

    private fun forwardLongClick(
        target: View,
        position: Int,
    ) {
        val listener = clickListener ?: return
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        listener.onItemLongClick(target, IndexedValue(position, getItem(position)))
    }

    companion object {
        private const val VIEW_TYPE_BANNER = 0
        private const val VIEW_TYPE_DEFAULT = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ReviewRecord>() {
            override fun areItemsTheSame(
                oldItem: ReviewRecord,
                newItem: ReviewRecord,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ReviewRecord,
                newItem: ReviewRecord,
            ): Boolean = oldItem == newItem
        }
    }
}
