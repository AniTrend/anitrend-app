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
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetComposerBinding
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.giphy.Giphy
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import io.wax911.emojify.EmojiManager
import io.wax911.emojify.parser.parseToUnicode
import java.util.*

/**
 * Created by max on 2017/12/02.
 * Composer widget for multiple feed types
 */

class ComposerWidget :
    FrameLayout,
    CustomView,
    View.OnClickListener {

    interface Listener {
        fun onSubmit(
            text: String,
            @KeyUtil.RequestType requestType: Int,
            onResult: (Boolean) -> Unit,
        )
    }

    private val binding by lazy {
        WidgetComposerBinding.inflate(
            getLayoutInflater(),
            this,
            true,
        )
    }

    private var recipient: UserBase? = null
    private var feedList: FeedList? = null
    private var feedReply: FeedReply? = null
    private var feedRecord: FeedRecord? = null
    private var feedReplyRecord: FeedReplyRecord? = null

    @KeyUtil.RequestType
    var requestType: Int = 0
    var lifecycle: Lifecycle? = null
    var itemClickListener: ItemClickListener<Any>? = null

    private var listener: Listener? = null
    private var recycled = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    val editor: EditText
        get() = binding.comment

    constructor(context: Context) :
        super(context) {
        onInit()
    }
    constructor(context: Context, attrs: AttributeSet?) :
        super(context, attrs) {
        onInit()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes) {
        onInit()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding.insertImage.setOnClickListener(this)
        binding.insertWebm.setOnClickListener(this)
        binding.insertLink.setOnClickListener(this)
        binding.insertYoutube.setOnClickListener(this)
        binding.insertGif.setOnClickListener(this)
        binding.widgetFlipper.setOnClickListener(this)
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    fun setModel(feedList: FeedList, @KeyUtil.RequestType requestType: Int) {
        this.feedList = feedList
        this.requestType = requestType
    }

    /**
     * Sending a new message to an existing user
     */
    fun setModel(recipient: UserBase, @KeyUtil.RequestType requestType: Int) {
        this.recipient = recipient
        this.requestType = requestType
    }

    /**
     * Editing a previously sent message to a user
     */
    fun setModel(feedList: FeedList) {
        this.feedList = feedList
    }

    fun setModel(feedReply: FeedReply, @KeyUtil.RequestType requestType: Int) {
        this.feedReply = feedReply
        this.requestType = requestType
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Preserves the legacy entity overloads for unrelated callers.
     */
    fun setModel(feedRecord: FeedRecord, @KeyUtil.RequestType requestType: Int) {
        this.feedRecord = feedRecord
        this.requestType = requestType
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Preserves the legacy entity overloads for unrelated callers.
     */
    fun setModel(feedReplyRecord: FeedReplyRecord, @KeyUtil.RequestType requestType: Int) {
        this.feedReplyRecord = feedReplyRecord
        this.requestType = requestType
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        recycled = true
        listener = null
        itemClickListener = null
    }

    @SuppressLint("SwitchIntDef")
    fun startRequestData() {
        if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
            binding.widgetFlipper.showNext()

            val formattedText = binding.comment.formattedText

            listener?.onSubmit(formattedText, requestType) { success ->
                if (recycled || !isAttachedToWindow) return@onSubmit
                lifecycle?.let { lifecycle ->
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        resetFlipperState()
                        if (success) {
                            binding.comment.text?.clear()
                        }
                    }
                }
            }
        } else {
            NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View) {
        if (itemClickListener != null) {
            itemClickListener?.onItemClick(view, IndexedValue(0, view as Any))
            when (view.id) {
                R.id.widget_flipper -> if (!binding.comment.isEmpty) {
                    startRequestData()
                } else {
                    NotifyUtil.makeText(context, R.string.warning_empty_input, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            NotifyUtil.makeText(context, R.string.dialog_action_null, Toast.LENGTH_SHORT).show()
        }
    }

    fun editBoxHasFocus(releaseFocus: Boolean): Boolean {
        val hasFocus = binding.comment.hasFocus()
        if (hasFocus && releaseFocus) {
            binding.comment.clearFocus()
        }
        return hasFocus
    }

    fun insertGiphy(giphy: Giphy) {
        val index = KeyUtil.GIPHY_LARGE_DOWN_SAMPLE
        val editor = binding.comment
        val start = editor.selectionStart
        giphy.images[index]?.apply {
            editor.editableText.insert(start, MarkDownUtil.convertImage(url.orEmpty()))
        }
    }

    fun appendText(textValue: String?) {
        val start = binding.comment.selectionStart
        val editable = binding.comment.editableText
        editable.insert(start, textValue.orEmpty())
    }

    fun setText(textValue: String?) {
        val manager = koinOf<EmojiManager>()
        val emojified: String? = when (!textValue.isNullOrBlank()) {
            true -> manager.parseToUnicode(textValue)
            else -> null
        }
        if (binding.comment.text.isNullOrBlank()) {
            if (!emojified.isNullOrBlank()) {
                binding.comment.setText(emojified)
            } else {
                binding.comment.setText(textValue)
            }
        } else {
            if (!emojified.isNullOrBlank()) {
                appendText(emojified)
            } else {
                appendText(textValue)
            }
        }
    }

    fun mentionUserFrom(feedReply: FeedReply) {
        val userName = feedReply.user?.name.orEmpty()
        appendText(String.format(Locale.getDefault(), "@%s ", userName))
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Preserves the legacy entity overload for unrelated callers.
     */
    fun mentionUserFrom(feedReplyRecord: FeedReplyRecord) {
        val userName = feedReplyRecord.user?.name.orEmpty()
        appendText(String.format(Locale.getDefault(), "@%s ", userName))
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
