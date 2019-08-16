package com.mxt.anitrend.base.custom.view.editor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import com.annimon.stream.IntPair
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.consumer.BaseConsumer
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetComposerBinding
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.util.*
import io.wax911.emojify.parser.EmojiParser
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * Created by max on 2017/12/02.
 * Composer widget for multiple feed types
 */

class ComposerWidget : FrameLayout, CustomView, View.OnClickListener, RetroCallback<ResponseBody> {

    private val binding by lazy {
        WidgetComposerBinding.inflate(
                CompatUtil.getLayoutInflater(context),
                this, true
        )
    }

    private val presenter by lazy {
        WidgetPresenter<ResponseBody>(context)
    }

    private var recipient: UserBase? = null
    private var feedList: FeedList? = null
    private var feedReply: FeedReply? = null

    @KeyUtil.RequestType
    var requestType: Int = 0
    var lifecycle: Lifecycle? = null
    var itemClickListener: ItemClickListener<Any>? = null

    val editor: EditText
        get() = binding.comment

    constructor(context: Context) :
            super(context) { onInit() }
    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs) { onInit() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) { onInit() }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) { onInit() }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding.onClickListener = this
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.LOADING_STATE)
            binding.widgetFlipper.displayedChild = WidgetPresenter.CONTENT_STATE
    }

    fun setModel(feedList: FeedList, @KeyUtil.RequestType requestType: Int) {
        this.feedList = feedList
        this.requestType = requestType
        // this.requestType = KeyUtils.MUT_SAVE_TEXT_FEED;
    }

    /**
     * Sending a new message to an existing user
     */
    fun setModel(recipient: UserBase, @KeyUtil.RequestType requestType: Int) {
        this.recipient = recipient
        this.requestType = requestType
        // this.requestType = KeyUtils.MUT_SAVE_MESSAGE_FEED;
    }

    /**
     * Editing a previously sent message to a user, we need to add teh feedId inorder to edit it
     */
    fun setModel(feedList: FeedList) {
        this.feedList = feedList
        // this.requestType = KeyUtils.MUT_SAVE_MESSAGE_FEED;
    }

    fun setModel(feedReply: FeedReply, @KeyUtil.RequestType requestType: Int) {
        this.feedReply = feedReply
        this.requestType = requestType
        // this.requestType = KeyUtils.MUT_SAVE_FEED_REPLY;
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
            presenter.onDestroy()
        itemClickListener = null
    }

    @SuppressLint("SwitchIntDef")
    fun startRequestData() {
        if (binding.widgetFlipper.displayedChild == WidgetPresenter.CONTENT_STATE) {
            binding.widgetFlipper.showNext()

            val queryContainer = GraphUtil.getDefaultQuery(false)

            when (requestType) {
                KeyUtil.MUT_SAVE_TEXT_FEED -> {
                    if (feedList != null) {
                        feedList?.text = binding.comment.formattedText
                        queryContainer.putVariable(KeyUtil.arg_id, feedList?.id)
                    }
                    queryContainer.putVariable(KeyUtil.arg_text, binding.comment.formattedText)
                }
                KeyUtil.MUT_SAVE_FEED_REPLY -> {
                    if (feedReply != null) {
                        feedReply?.setText(binding.comment.formattedText)
                        queryContainer.putVariable(KeyUtil.arg_id, feedReply?.id)
                    }
                    queryContainer.putVariable(KeyUtil.arg_activityId, feedList?.id)
                            .putVariable(KeyUtil.arg_text, binding.comment.formattedText)
                }
                KeyUtil.MUT_SAVE_MESSAGE_FEED -> {
                    if (feedList != null) {
                        feedList?.text = binding.comment.formattedText
                        queryContainer.putVariable(KeyUtil.arg_id, feedList?.id)
                    }
                    queryContainer.putVariable(KeyUtil.arg_recipientId, recipient?.id)
                    queryContainer.putVariable(KeyUtil.arg_message, binding.comment.formattedText)
                }
            }

            presenter.params?.putParcelable(KeyUtil.arg_graph_params, queryContainer)
            presenter.requestData(requestType, context, this)
        } else
            NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(view: View) {
        if (itemClickListener != null) {
            itemClickListener?.onItemClick(view, null)
            when (view.id) {
                R.id.widget_flipper -> if (!binding.comment.isEmpty)
                    startRequestData()
                else
                    NotifyUtil.makeText(context, R.string.warning_empty_input, Toast.LENGTH_SHORT).show()
            }
        } else
            NotifyUtil.makeText(context, R.string.dialog_action_null, Toast.LENGTH_SHORT).show()
    }

    fun editBoxHasFocus(releaseFocus: Boolean): Boolean {
        val hasFocus = binding.comment.hasFocus()
        if (hasFocus && releaseFocus)
            binding.comment.clearFocus()
        return hasFocus
    }

    /**
     * Invoked for a received HTTP response.
     *
     *
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call [Response.isSuccessful] to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            resetFlipperState()
            if (response.isSuccessful) {
                binding.comment.text?.clear()
                when (requestType) {
                    KeyUtil.MUT_SAVE_TEXT_FEED -> if (feedList != null)
                        presenter.notifyAllListeners(BaseConsumer<FeedList>(requestType, feedList), false)
                    else
                        presenter.notifyAllListeners(BaseConsumer<FeedList>(requestType), false)
                    KeyUtil.MUT_SAVE_FEED_REPLY -> if (feedReply != null)
                        presenter.notifyAllListeners(BaseConsumer<FeedReply>(requestType, feedReply), false)
                    else
                        presenter.notifyAllListeners(BaseConsumer<FeedReply>(requestType), false)
                    KeyUtil.MUT_SAVE_MESSAGE_FEED -> if (feedList != null)
                        presenter.notifyAllListeners(BaseConsumer<FeedList>(requestType, feedList), false)
                    else
                        presenter.notifyAllListeners(BaseConsumer<FeedList>(requestType), false)
                }
            } else
                NotifyUtil.makeText(context, ErrorUtil.getError(response), Toast.LENGTH_SHORT).show()
            presenter.onDestroy()
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
        if (lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true) {
            resetFlipperState()
            throwable.printStackTrace()
            NotifyUtil.makeText(context, throwable.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onGiphyClicked(pair: IntPair<Giphy>) {
        val index = KeyUtil.GIPHY_LARGE_DOWN_SAMPLE
        val editor = binding.comment
        val start = editor.selectionStart
        pair.second.images[index]?.apply {
            editor.editableText.insert(start, MarkDownUtil.convertImage(url))
        }
    }

    fun appendText(textValue: String?) {
        val start = binding.comment.selectionStart
        val editable = binding.comment.editableText
        editable.insert(start, textValue)
    }

    fun setText(textValue: String?) {
        val emojified: String? = when (!textValue.isNullOrBlank()) {
            true -> EmojiParser.parseToUnicode(textValue)
            else -> null
        }
        if (binding.comment.text.isNullOrBlank()) {
            if (!emojified.isNullOrBlank())
                binding.comment.setText(emojified)
            else
                binding.comment.setText(textValue)
        } else {
            if (!emojified.isNullOrBlank())
                appendText(emojified)
            else
                appendText(textValue)
        }
    }

    fun mentionUserFrom(feedReply: FeedReply) {
        val user = feedReply.user.name
        appendText(String.format(Locale.getDefault(), "@%s ", user))
    }
}
