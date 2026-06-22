package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetDeleteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.anilist.meta.DeleteState
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.graphql.GraphUtil
import com.mxt.anitrend.util.graphql.apiError
import co.anitrend.retrofit.graphql.model.request.QueryContainerBuilder
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber

class StatusDeleteWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    RetroCallback<DeleteState>,
    View.OnClickListener {

    private lateinit var binding: WidgetDeleteBinding
    private var presenter: WidgetPresenter<DeleteState>? = null
    @KeyUtil.RequestType
    private var requestType: Int = 0
    private var feedList: FeedList? = null
    private var feedReply: FeedReply? = null

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        presenter = WidgetPresenter(context)
        binding = WidgetDeleteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetDelete.setCompoundDrawablesWithIntrinsicBounds(
            context.getCompatDrawable(R.drawable.ic_delete_red_600_18dp),
            null,
            null,
            null
        )
        binding.widgetFlipper.setOnClickListener(this)
    }

    private fun setParameters(feedId: Long, @KeyUtil.RequestType requestType: Int) {
        this.requestType = requestType
        val queryContainer: QueryContainerBuilder = GraphUtil.getDefaultQuery(false)
            .putVariable(KeyUtil.arg_id, feedId)
        presenter?.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
    }

    fun setModel(feedList: FeedList, @KeyUtil.RequestType requestType: Int) {
        setParameters(feedList.id, requestType)
        this.feedList = feedList
    }

    fun setModel(feedReply: FeedReply, @KeyUtil.RequestType requestType: Int) {
        setParameters(feedReply.id, requestType)
        this.feedReply = feedReply
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        resetFlipperState()
        presenter?.onDestroy()
        presenter = null
        feedReply = null
        feedList = null
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
    }

    override fun onClick(view: View) {
        DialogUtil.createMessage(
            context,
            R.string.dialog_title_delete_activity,
            R.string.dialog_message_delete_activity
        ) { _, which ->
            when (which) {
                DialogAction.POSITIVE -> {
                    if (view.id == R.id.widget_flipper) {
                        if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
                            binding.widgetFlipper.showNext()
                            presenter?.requestData(requestType, context, this)
                        } else {
                            NotifyUtil.makeText(
                                context,
                                R.string.busy_please_wait,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                DialogAction.NEGATIVE -> {
                    NotifyUtil.makeText(
                        context,
                        R.string.canceled_by_user,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Unit
            }
        }
    }

    /**
     * Invoked for a received HTTP response.
     */
    override fun onResponse(call: Call<DeleteState>, response: Response<DeleteState>) {
        try {
            val deleteState = response.body()
            if (response.isSuccessful && deleteState != null) {
                resetFlipperState()
                if (deleteState.isDeleted) {
                    when (requestType) {
                        KeyUtil.MUT_DELETE_FEED ->
                            presenter?.notifyAllListeners(BaseConsumer(requestType, feedList), false)
                        KeyUtil.MUT_DELETE_FEED_REPLY ->
                            presenter?.notifyAllListeners(BaseConsumer(requestType, feedReply), false)
                    }
                } else {
                    NotifyUtil.makeText(
                        context,
                        R.string.text_error_request,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Timber.w(response.apiError())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     */
    override fun onFailure(call: Call<DeleteState>, throwable: Throwable) {
        try {
            Timber.w(throwable)
            resetFlipperState()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
