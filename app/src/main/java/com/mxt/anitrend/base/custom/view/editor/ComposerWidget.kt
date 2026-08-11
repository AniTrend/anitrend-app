package com.mxt.anitrend.base.custom.view.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.view.container.CardViewBase
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
import com.google.android.material.color.MaterialColors
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
    private var isSheetLayout = false
    private var isReplyContext = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    val editor: EditText
        get() = binding.comment

    constructor(context: Context) :
        super(context) {
        readLayoutMode(null)
        onInit()
    }
    constructor(context: Context, attrs: AttributeSet?) :
        super(context, attrs) {
        readLayoutMode(attrs)
        onInit()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        readLayoutMode(attrs)
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes) {
        readLayoutMode(attrs)
        onInit()
    }

    private fun readLayoutMode(attrs: AttributeSet?) {
        isSheetLayout = attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.ComposerWidget).use { typedArray ->
                typedArray.getBoolean(R.styleable.ComposerWidget_sheetLayout, false)
            }
        } ?: false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.composerSurface.setCardBackgroundColor(
            if (parent is CardViewBase) {
                Color.TRANSPARENT
            } else {
                MaterialColors.getColor(this, R.attr.colorSurfaceContainer)
            },
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        applySheetAwareLayoutParams()
        binding.insertImage.setOnClickListener(this)
        binding.insertWebm.setOnClickListener(this)
        binding.insertLink.setOnClickListener(this)
        binding.insertYoutube.setOnClickListener(this)
        binding.insertGif.setOnClickListener(this)
        binding.insertOverflow.setOnClickListener(this)
        binding.insertEmoticon.setOnClickListener(this)
        binding.widgetFlipper.setOnClickListener(this)
        binding.postAction.setOnClickListener(this)
        binding.replySendIcon.setOnClickListener(this)
        updateActionVisibility()
        binding.comment.doAfterTextChanged {
            if (binding.commentLayout.error != null) {
                binding.commentLayout.error = null
            }
        }
    }

    override fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int,
    ) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateActionVisibility()
    }

    private fun updateActionVisibility() {
        if (width <= 0) return
        val narrowWidth = width < resources.getDimensionPixelSize(R.dimen.composer_action_breakpoint)
        binding.insertOverflow.visibility = if (narrowWidth) View.VISIBLE else View.GONE
        binding.insertLink.visibility = if (narrowWidth) View.GONE else View.VISIBLE
        binding.insertYoutube.visibility = if (narrowWidth) View.GONE else View.VISIBLE
        binding.insertGif.visibility = if (narrowWidth) View.GONE else View.VISIBLE
        binding.postAction.visibility = if (isReplyContext) View.GONE else View.VISIBLE
        binding.replySendIcon.visibility = if (isReplyContext) View.VISIBLE else View.GONE
    }

    private fun updateReplyContext(reply: Boolean, name: String?) {
        isReplyContext = reply
        binding.replyRecipientChip.apply {
            visibility = if (reply && !name.isNullOrBlank()) View.VISIBLE else View.GONE
            text = name?.let { context.getString(R.string.composer_reply_to, it) }
            contentDescription = name?.let { context.getString(R.string.composer_reply_to, it) }
        }
        binding.comment.hint = context.getString(
            if (reply) R.string.composer_reply_hint else R.string.text_hint_comment,
        )
        binding.comment.minLines = if (reply) 1 else 3
        updateActionVisibility()
    }

    private fun applySheetAwareLayoutParams() {
        binding.root.layoutParams?.let { params ->
            params.height =
                if (isSheetLayout) {
                    ViewGroup.LayoutParams.MATCH_PARENT
                } else {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            binding.root.layoutParams = params
        }
        val containerParams = binding.composerContainer.layoutParams ?: return
        if (isSheetLayout) {
            containerParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            containerParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        binding.composerContainer.layoutParams = containerParams

        val editorParams = binding.composerEditorScroll.layoutParams as? LinearLayout.LayoutParams
            ?: return
        if (isSheetLayout) {
            editorParams.height = 0
            editorParams.weight = 1f
        } else {
            editorParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            editorParams.weight = 0f
        }
        binding.composerEditorScroll.layoutParams = editorParams
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
            binding.widgetFlipper.contentDescription = context.getString(R.string.composer_send_description)
        }
    }

    fun setModel(feedList: FeedList, @KeyUtil.RequestType requestType: Int) {
        this.feedList = feedList
        this.requestType = requestType
        updateReplyContext(false, null)
    }

    /**
     * Sending a new message to an existing user
     */
    fun setModel(recipient: UserBase, @KeyUtil.RequestType requestType: Int) {
        this.recipient = recipient
        this.requestType = requestType
        updateReplyContext(false, null)
    }

    /**
     * Editing a previously sent message to a user
     */
    fun setModel(feedList: FeedList) {
        this.feedList = feedList
        updateReplyContext(false, null)
    }

    fun setModel(feedReply: FeedReply, @KeyUtil.RequestType requestType: Int) {
        this.feedReply = feedReply
        this.requestType = requestType
        updateReplyContext(true, feedReply.user?.name)
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Preserves the legacy entity overloads for unrelated callers.
     */
    fun setModel(feedRecord: FeedRecord, @KeyUtil.RequestType requestType: Int) {
        this.feedRecord = feedRecord
        this.requestType = requestType
        updateReplyContext(requestType == KeyUtil.MUT_SAVE_FEED_REPLY, feedRecord.user?.name)
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Preserves the legacy entity overloads for unrelated callers.
     */
    fun setModel(feedReplyRecord: FeedReplyRecord, @KeyUtil.RequestType requestType: Int) {
        this.feedReplyRecord = feedReplyRecord
        this.requestType = requestType
        updateReplyContext(true, feedReplyRecord.user?.name)
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
        if (binding.comment.isEmpty) {
            binding.commentLayout.error = context.getString(R.string.warning_empty_input)
            return
        }
        if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
            binding.widgetFlipper.showNext()
            binding.widgetFlipper.contentDescription = context.getString(R.string.composer_sending_description)

            val formattedText = binding.comment.formattedText

            listener?.onSubmit(formattedText, requestType) { success ->
                if (recycled || !isAttachedToWindow) return@onSubmit
                lifecycle?.let { lifecycle ->
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        resetFlipperState()
                        if (success) {
                            binding.comment.text?.clear()
                        } else {
                            binding.commentLayout.error = context.getString(R.string.text_error_request)
                        }
                    }
                }
            }
        } else {
            NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.insert_overflow) {
            showOverflowMenu(view)
            return
        }
        if (view.id == R.id.post_action || view.id == R.id.reply_send_icon) {
            itemClickListener?.onItemClick(
                binding.widgetFlipper,
                IndexedValue(0, binding.widgetFlipper as Any),
            )
            startRequestData()
            return
        }
        if (view.id == R.id.widget_flipper && binding.comment.isEmpty) {
            binding.commentLayout.error = context.getString(R.string.warning_empty_input)
            return
        }
        if (itemClickListener != null) {
            itemClickListener?.onItemClick(view, IndexedValue(0, view as Any))
            when (view.id) {
                R.id.widget_flipper -> if (!binding.comment.isEmpty) {
                    startRequestData()
                }
            }
        } else {
            NotifyUtil.makeText(context, R.string.dialog_action_null, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOverflowMenu(anchor: View) {
        PopupMenu(context, anchor).apply {
            menu.add(0, R.id.insert_link, 0, R.string.composer_add_link_description)
            menu.add(0, R.id.insert_youtube, 1, R.string.composer_add_youtube_description)
            menu.add(0, R.id.insert_gif, 2, R.string.composer_add_gif_description)
            setOnMenuItemClickListener { item ->
                itemClickListener?.onItemClick(
                    binding.root.findViewById(item.itemId),
                    IndexedValue(0, binding.root.findViewById<View>(item.itemId)),
                )
                true
            }
        }.show()
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
