package com.mxt.anitrend.adapter.recycler.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewHolder
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.FeedSlideRow
import com.mxt.anitrend.util.markdown.RegexUtil
import java.util.Locale

private data class FeedSlideState(
    val imageUrl: String,
    val centerCrop: Boolean,
    val showPlayback: Boolean,
    val transitionName: String?,
)

/**
 * Created by max on 2017/11/25.
 * image preview adapter
 */
class ImagePreviewAdapter(
    private val contentTypes: List<String>,
    context: Context,
) : RecyclerViewAdapter<String>(context) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewHolder<String> {
        val composeView =
            ComposeView(parent.context).apply {
                id = R.id.feed_status_image
                layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
            }
        return PreviewHolder(composeView)
    }

    override fun getFilter(): Filter? = null

    private inner class PreviewHolder(
        private val composeView: ComposeView,
    ) : RecyclerViewHolder<String>(composeView) {
        private val slideState = mutableStateOf<FeedSlideState?>(null)

        init {
            composeView.setContent {
                AniTrendTheme {
                    slideState.value?.let { slide ->
                        FeedSlideRow(
                            imageUrl = slide.imageUrl,
                            centerCrop = slide.centerCrop,
                            showPlayback = slide.showPlayback,
                            roundedCorners = true,
                            onClick = { onClick(composeView) },
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(model: String) {
            val position = bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val slide =
                when (contentTypes[position].lowercase(Locale.getDefault())) {
                    RegexUtil.KEY_IMG -> FeedSlideState(model, centerCrop = false, showPlayback = false, transitionName = model.takeIf(String::isNotBlank))
                    RegexUtil.KEY_YOU -> FeedSlideState(RegexUtil.getYoutubeThumb(model), centerCrop = true, showPlayback = true, transitionName = null)
                    else -> FeedSlideState(RegexUtil.NO_THUMBNAIL, centerCrop = true, showPlayback = true, transitionName = null)
                }
            slideState.value = slide
            ViewCompat.setTransitionName(composeView, slide.transitionName)
        }

        override fun onViewRecycled() {
            slideState.value = null
            ViewCompat.setTransitionName(composeView, null)
        }

        override fun onClick(v: View) {
            performClick(clickListener, data, v)
        }

        override fun onLongClick(v: View): Boolean = performLongClick(clickListener, data, v)
    }
}
