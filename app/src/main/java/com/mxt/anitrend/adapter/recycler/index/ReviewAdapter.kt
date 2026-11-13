package com.mxt.anitrend.adapter.recycler.index

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.data.store.mutation.MutationResult
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.graphql.generated.ReviewRating
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.review.ReviewBannerRow
import com.mxt.anitrend.ui.review.ReviewSeriesRow
import com.mxt.anitrend.ui.review.ReviewVoteRowState
import com.mxt.anitrend.util.NotifyUtil

/** Typed image actions dispatched from review rows to their owning destination. */
enum class ReviewImageAction {
    OPEN_MEDIA,
    OPEN_PROFILE,
    LONG_PRESS_MEDIA,
}

/**
 * Created by max on 2017/10/30.
 * Media review adapter
 *
 * Immutable [ListAdapter] over [ReviewRecord]: items are submitted whole, never mutated
 * after submission, and diffed on stable review ids. Both rows are Compose-hosted;
 * the adapter retains vote state, mutation routing, and existing action owners.
 */
class ReviewAdapter(
    private val currentUser: UserBase?,
    private val onRateReviewAction: (Long, ReviewRating?) -> Unit,
    private val onReviewImageAction: (ReviewImageAction, ReviewRecord) -> Unit,
    private val isMediaType: Boolean = false,
) : ListAdapter<ReviewRecord, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var clickListener: ItemClickListener<ReviewRecord>? = null

    /**
     * Currently bound holders keyed by stable review id, so mutation outcomes arriving
     * after the list settled can still converge the visible vote controls without
     * re-submitting the immutable list.
     */
    private val holderRegistry = ReviewHolderRegistry<RecyclerView.ViewHolder>()

    /** Routes mutation outcomes to the holder currently bound to [reviewId]. */
    fun onRateReviewResult(
        reviewId: Long,
        result: MutationResult,
    ) {
        val holder = holderRegistry.holderFor(reviewId) ?: return
        when (holder) {
            is ReviewBanner -> holder.onRateReviewResult(result)
            is ReviewDefault -> holder.onRateReviewResult(result)
        }
    }

    override fun getItemViewType(position: Int): Int = if (!isMediaType) VIEW_TYPE_BANNER else VIEW_TYPE_DEFAULT

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder = if (viewType == VIEW_TYPE_BANNER) {
        ReviewBanner(
            ComposeView(parent.context).apply {
                id = R.id.container
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    val margin = parent.context.resources.getDimensionPixelSize(R.dimen.lg_margin)
                    setMargins(margin, margin, margin, margin)
                }
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            },
        )
    } else {
        ReviewDefault(
            ComposeView(parent.context).apply {
                id = R.id.container
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply {
                        val margin = parent.context.resources.getDimensionPixelSize(R.dimen.lg_margin)
                        setMargins(margin, margin, margin, margin)
                    }
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            },
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val record = getItem(position)
        holderRegistry.onBound(record.id, holder)
        when (holder) {
            is ReviewBanner -> holder.bind(record)
            is ReviewDefault -> holder.bind(record)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        holderRegistry.onRecycled(holder)
        when (holder) {
            is ReviewBanner -> holder.recycle()
            is ReviewDefault -> holder.recycle()
        }
    }

    inner class ReviewBanner(
        private val composeView: ComposeView,
    ) : RecyclerView.ViewHolder(composeView) {
        private val model = mutableStateOf<ReviewRecord?>(null)
        private val voteController = VoteController(composeView.context)
        private var readMoreView: MaterialTextView? = null

        init {
            composeView.setContent {
                AniTrendTheme {
                    model.value?.let { review ->
                        ReviewBannerRow(
                            review = review,
                            voteState = voteController.state.value,
                            onUpVoteClick = { voteController.onVote(ReviewRating.UP_VOTE) },
                            onDownVoteClick = { voteController.onVote(ReviewRating.DOWN_VOTE) },
                            onImageClick = {
                                forwardImageAction(ReviewImageAction.OPEN_MEDIA, bindingAdapterPosition)
                            },
                            onImageLongClick = {
                                forwardImageAction(ReviewImageAction.LONG_PRESS_MEDIA, bindingAdapterPosition)
                            },
                            onReadMoreClick = {
                                readMoreView?.let { forwardClick(it, bindingAdapterPosition) }
                            },
                            onReadMoreViewCreated = { readMoreView = it },
                            onReadMoreViewReleased = { view ->
                                view.setOnClickListener(null)
                                if (readMoreView === view) readMoreView = null
                            },
                        )
                    }
                }
            }
        }

        fun bind(model: ReviewRecord) {
            voteController.bind(model, R.color.white)
            this.model.value = model
        }

        fun onRateReviewResult(result: MutationResult) {
            voteController.onRateReviewResult(result)
        }

        fun recycle() {
            voteController.onViewRecycled()
        }
    }

    inner class ReviewDefault(
        private val composeView: ComposeView,
    ) : RecyclerView.ViewHolder(composeView) {
        private val model = mutableStateOf<ReviewRecord?>(null)
        private val voteController = VoteController(composeView.context)
        private var readMoreView: MaterialTextView? = null

        init {
            composeView.setContent {
                AniTrendTheme {
                    model.value?.let { review ->
                        ReviewSeriesRow(
                            review = review,
                            voteState = voteController.state.value,
                            onUpVoteClick = { voteController.onVote(ReviewRating.UP_VOTE) },
                            onDownVoteClick = { voteController.onVote(ReviewRating.DOWN_VOTE) },
                            onProfileClick = {
                                forwardImageAction(ReviewImageAction.OPEN_PROFILE, bindingAdapterPosition)
                            },
                            onReadMoreClick = {
                                readMoreView?.let { forwardClick(it, bindingAdapterPosition) }
                            },
                            onReadMoreViewCreated = { readMoreView = it },
                            onReadMoreViewReleased = { view ->
                                view.setOnClickListener(null)
                                if (readMoreView === view) readMoreView = null
                            },
                            onCoverLongClick = {
                                forwardImageAction(ReviewImageAction.LONG_PRESS_MEDIA, bindingAdapterPosition)
                            },
                        )
                    }
                }
            }
        }

        fun bind(model: ReviewRecord) {
            voteController.bind(model, 0)
            this.model.value = model
        }

        fun onRateReviewResult(result: MutationResult) {
            voteController.onRateReviewResult(result)
        }

        fun recycle() {
            voteController.onViewRecycled()
        }
    }

    private inner class VoteController(
        private val context: Context,
    ) {
        val state = mutableStateOf(ReviewVoteRowState())
        private val mainHandler = Handler(Looper.getMainLooper())
        private val loadingFallback = Runnable { resetLoadingState() }
        private var model: ReviewRecord? = null

        fun bind(
            model: ReviewRecord,
            @ColorRes colorStyle: Int,
        ) {
            resetLoadingState()
            this.model = model
            state.value =
                ReviewVoteRowState(
                    upVoteCount = model.rating,
                    downVoteCount = (model.ratingAmount - model.rating).coerceAtLeast(0),
                    userRating = model.userRating,
                    colorStyle = colorStyle,
                )
        }

        fun onRateReviewResult(result: MutationResult) {
            if (result is MutationResult.Failure) {
                NotifyUtil.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
            resetLoadingState()
        }

        fun onViewRecycled() {
            resetLoadingState()
            model = null
            state.value = ReviewVoteRowState()
        }

        fun onVote(target: ReviewRating) {
            val currentModel = model ?: return
            if (currentUser == null) {
                NotifyUtil
                    .makeText(
                        context,
                        R.string.info_login_req,
                        R.drawable.ic_group_add_grey_600_18dp,
                        Toast.LENGTH_SHORT,
                    ).show()
                return
            }

            val currentState = state.value
            val isLoading =
                when (target) {
                    ReviewRating.UP_VOTE -> currentState.upVoteLoading
                    ReviewRating.DOWN_VOTE -> currentState.downVoteLoading
                    else -> false
                }
            if (isLoading) {
                NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
                return
            }

            state.value =
                when (target) {
                    ReviewRating.UP_VOTE -> currentState.copy(upVoteLoading = true)
                    ReviewRating.DOWN_VOTE -> currentState.copy(downVoteLoading = true)
                    else -> currentState
                }
            mainHandler.removeCallbacks(loadingFallback)
            mainHandler.postDelayed(loadingFallback, LOADING_FALLBACK_TIMEOUT_MS)

            val rating = if (currentModel.userRating == target.name) ReviewRating.NO_VOTE else target
            onRateReviewAction(currentModel.id, rating)
        }

        private fun resetLoadingState() {
            mainHandler.removeCallbacks(loadingFallback)
            val currentState = state.value
            if (currentState.upVoteLoading || currentState.downVoteLoading) {
                state.value = currentState.copy(upVoteLoading = false, downVoteLoading = false)
            }
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

    private fun forwardImageAction(
        action: ReviewImageAction,
        position: Int,
    ) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        onReviewImageAction(action, getItem(position))
    }

    companion object {
        private const val VIEW_TYPE_BANNER = 0
        private const val VIEW_TYPE_DEFAULT = 1
        private const val LOADING_FALLBACK_TIMEOUT_MS = 10_000L

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

/**
 * Tracks which holder is currently bound to which review id so mutation outcomes can be
 * routed to the visible vote control without re-submitting the immutable list.
 *
 * A holder may be rebound from one review to another without being recycled first, so
 * every bind removes the holder's prior mapping and records the review id it is now bound
 * to. Outcome routing resolves through [holderFor], which only returns a holder that is
 * still bound to the requested review id; a stale outcome for a previously bound review
 * can therefore never reach a holder that was rebound to a different review.
 */
internal class ReviewHolderRegistry<H : Any> {

    private val holdersByReviewId = mutableMapOf<Long, H>()
    private val boundReviewIdByHolder = mutableMapOf<H, Long>()

    fun onBound(
        reviewId: Long,
        holder: H,
    ) {
        boundReviewIdByHolder.remove(holder)?.let { priorReviewId ->
            if (holdersByReviewId[priorReviewId] === holder) {
                holdersByReviewId.remove(priorReviewId)
            }
        }
        boundReviewIdByHolder[holder] = reviewId
        holdersByReviewId[reviewId] = holder
    }

    fun onRecycled(holder: H) {
        boundReviewIdByHolder.remove(holder)?.let { reviewId ->
            if (holdersByReviewId[reviewId] === holder) {
                holdersByReviewId.remove(reviewId)
            }
        }
    }

    fun holderFor(reviewId: Long): H? {
        val holder = holdersByReviewId[reviewId] ?: return null
        return holder.takeIf { boundReviewIdByHolder[it] == reviewId }
    }
}
