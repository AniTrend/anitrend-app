package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.mxt.anitrend.adapter.recycler.detail.ImagePreviewAdapter
import com.mxt.anitrend.base.custom.recycler.RecyclerViewAdapter
import com.mxt.anitrend.base.interfaces.event.ItemClickListener
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetStatusBinding
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.util.CenterSnapUtil
import com.mxt.anitrend.util.markdown.RegexUtil
import java.util.ArrayList

/**
 * Created by max on 2017/11/25.
 */
class StatusContentWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr),
    CustomView,
    ItemClickListener<String>,
    CenterSnapUtil.PositionChangeListener {
    private var contentLinks: MutableList<String>? = null
    private var contentTypes: MutableList<String>? = null
    private lateinit var binding: WidgetStatusBinding

    var onImagePreviewRequested: ((String) -> Unit)? = null
    var onVideoPreviewRequested: ((String) -> Unit)? = null
    var onYoutubeRequested: ((String) -> Unit)? = null

    init {
        onInit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : this(context, attrs, defStyleAttr)

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetStatusBinding.inflate(LayoutInflater.from(context), this, true)
        binding.widgetStatusRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.widgetStatusRecycler.isNestedScrollingEnabled = true
        binding.widgetStatusRecycler.addOnItemTouchListener(
            object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(
                    rv: RecyclerView,
                    e: MotionEvent,
                ): Boolean {
                    val action = e.actionMasked
                    if (action == MotionEvent.ACTION_MOVE) {
                        rv.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    return false
                }
            },
        )
        val snapHelper: SnapHelper = CenterSnapUtil(this)
        snapHelper.attachToRecyclerView(binding.widgetStatusRecycler)
    }

    fun setModel(model: FeedList) {
        findMediaAttachments(model.text)
    }

    fun setModel(model: FeedReply) {
        findMediaAttachments(model.reply)
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Delegates to the same media-attachment rendering path, preserving all
     * visual and click behavior. Legacy entity overloads stay untouched.
     */
    fun setModel(model: FeedRecord) {
        findMediaAttachments(model.text)
    }

    /**
     * Record-typed additive overload for the comment/reply migration lane.
     * Delegates to the same media-attachment rendering path, preserving all
     * visual and click behavior. Legacy entity overloads stay untouched.
     */
    fun setModel(model: FeedReplyRecord) {
        findMediaAttachments(model.reply)
    }

    fun setTextData(textData: String?) {
        findMediaAttachments(textData)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        contentLinks = null
        contentTypes = null
        onImagePreviewRequested = null
        onVideoPreviewRequested = null
        onYoutubeRequested = null
    }

    private fun findMediaAttachments(value: String?) {
        val textValue = value ?: return
        if (!TextUtils.isEmpty(textValue)) {
            val matcher = RegexUtil.findMedia(textValue)
            contentLinks = ArrayList()
            contentTypes = ArrayList()
            while (matcher.find()) {
                val gc = matcher.groupCount()
                val tag = matcher.group(gc - 1)
                val media = matcher.group(gc)
                if (tag != null && media != null) {
                    contentTypes?.add(tag)
                    if (RegexUtil.KEY_YOU == tag) {
                        contentLinks?.add(RegexUtil.buildYoutube(media.replace("(", "").replace(")", "")))
                    } else {
                        contentLinks?.add(media.replace("(", "").replace(")", ""))
                    }
                }
            }
        }
        constructAdditionalViews()
    }

    private fun constructAdditionalViews() {
        val links = contentLinks
        if (!links.isNullOrEmpty()) {
            val types = contentTypes ?: return
            val previewAdapter: RecyclerViewAdapter<String> = ImagePreviewAdapter(types, context)
            previewAdapter.onItemsInserted(links)
            previewAdapter.setClickListener(this)
            binding.widgetStatusRecycler.adapter = previewAdapter

            if (previewAdapter.itemCount < 2) {
                binding.widgetStatusIndicator.visibility = GONE
            } else {
                binding.widgetStatusIndicator.visibility = VISIBLE
                binding.widgetStatusIndicator.maximum = previewAdapter.itemCount
                binding.widgetStatusIndicator.setCurrentPosition(1)
            }
            binding.widgetSlideHolder.visibility = VISIBLE
        } else {
            binding.widgetSlideHolder.visibility = GONE
        }
    }

    override fun onPageChanged(currentPage: Int) {
        binding.widgetStatusIndicator.setCurrentPosition(currentPage)
    }

    override fun onItemClick(
        target: View,
        data: IndexedValue<String>,
    ) {
        val type = contentTypes?.getOrNull(data.index)?.lowercase() ?: return
        val content = data.value
        when (type) {
            RegexUtil.KEY_IMG -> onImagePreviewRequested?.invoke(content)
            RegexUtil.KEY_WEB -> onVideoPreviewRequested?.invoke(content)
            RegexUtil.KEY_YOU -> onYoutubeRequested?.invoke(content)
        }
    }

    override fun onItemLongClick(
        target: View,
        data: IndexedValue<String>,
    ) = Unit
}
