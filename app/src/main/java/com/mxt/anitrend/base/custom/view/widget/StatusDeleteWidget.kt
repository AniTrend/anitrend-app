package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetDeleteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber

class StatusDeleteWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    interface Listener {
        fun onDeleteFeed(
            feedId: Long,
            @KeyUtil.RequestType requestType: Int,
            onResult: (Result<DeleteState>) -> Unit,
        )
    }

    private lateinit var binding: WidgetDeleteBinding

    @KeyUtil.RequestType
    private var requestType: Int = 0
    private var feedList: FeedList? = null
    private var feedReply: FeedReply? = null
    private var listener: Listener? = null
    private var recycled = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetDeleteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetDelete.setCompoundDrawablesWithIntrinsicBounds(
            context.getCompatDrawable(R.drawable.ic_delete_red_600_18dp),
            null,
            null,
            null,
        )
        binding.widgetFlipper.setOnClickListener(this)
    }

    fun setModel(
        feedList: FeedList,
        @KeyUtil.RequestType requestType: Int,
    ) {
        this.requestType = requestType
        this.feedList = feedList
    }

    fun setModel(
        feedReply: FeedReply,
        @KeyUtil.RequestType requestType: Int,
    ) {
        this.requestType = requestType
        this.feedReply = feedReply
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        recycled = true
        listener = null
        resetFlipperState()
        feedReply = null
        feedList = null
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    override fun onClick(view: View) {
        DialogUtil.createMessage(
            context,
            R.string.dialog_title_delete_activity,
            R.string.dialog_message_delete_activity,
        ) { _, _ ->
            if (view.id == R.id.widget_flipper) {
                if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                    val feedId = feedList?.id ?: feedReply?.id ?: run {
                        resetFlipperState()
                        return@createMessage
                    }
                    listener?.onDeleteFeed(feedId, requestType) { result ->
                        if (recycled || !isAttachedToWindow) return@onDeleteFeed
                        result.onSuccess {
                            resetFlipperState()
                        }.onFailure {
                            resetFlipperState()
                            Timber.w(it)
                        }
                    }
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
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
