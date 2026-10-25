package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetStatusBinding
import com.mxt.anitrend.domain.feed.model.FeedRecord
import com.mxt.anitrend.domain.feed.model.FeedReplyRecord
import com.mxt.anitrend.model.entity.anilist.FeedList
import com.mxt.anitrend.model.entity.anilist.FeedReply
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.media.FeedAttachment
import com.mxt.anitrend.ui.media.FeedAttachmentCarousel
import com.mxt.anitrend.util.markdown.RegexUtil
import java.util.Locale

/** Renders the media attachments embedded in a status or reply. */
class StatusContentWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr),
    CustomView {
    private lateinit var binding: WidgetStatusBinding
    private val attachments = mutableStateOf<List<FeedAttachment>>(emptyList())

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

    override fun onInit() {
        binding = WidgetStatusBinding.inflate(LayoutInflater.from(context), this, true)
        binding.widgetStatusComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AniTrendTheme {
                    val current = attachments.value
                    if (current.isNotEmpty()) {
                        // A different status starts at its first attachment; unrelated rebinds retain the page.
                        key(current) {
                            FeedAttachmentCarousel(
                                attachments = current,
                                onOpen = ::openAttachment,
                                onPageChanged = ::onPageChanged,
                            )
                        }
                    }
                }
            }
        }
    }

    fun setModel(model: FeedList) = setTextData(model.text)

    fun setModel(model: FeedReply) = setTextData(model.reply)

    fun setModel(model: FeedRecord) = setTextData(model.text)

    fun setModel(model: FeedReplyRecord) = setTextData(model.reply)

    fun setTextData(textData: String?) {
        val next = ArrayList<FeedAttachment>()
        if (!textData.isNullOrEmpty()) {
            val matcher = RegexUtil.findMedia(textData)
            while (matcher.find()) {
                val count = matcher.groupCount()
                val type = matcher.group(count - 1)?.lowercase(Locale.ROOT)
                val media = matcher.group(count)
                if (type != null && media != null) {
                    val link = media.replace("(", "").replace(")", "")
                    val url = if (type == RegexUtil.KEY_YOU) RegexUtil.buildYoutube(link) else link
                    next.add(FeedAttachment.from(type, url))
                }
            }
        }
        val changed = attachments.value != next
        attachments.value = next
        binding.widgetSlideHolder.visibility = if (next.isEmpty()) GONE else VISIBLE
        binding.widgetStatusIndicator.visibility = if (next.size < 2) GONE else VISIBLE
        binding.widgetStatusIndicator.maximum = next.size
        if (changed) onPageChanged(0)
    }

    private fun onPageChanged(page: Int) {
        binding.widgetStatusIndicator.setCurrentPosition(page + 1)
        val attachment = attachments.value.getOrNull(page)
        binding.widgetStatusComposeView.transitionName =
            attachment?.takeIf { it.type == RegexUtil.KEY_IMG }?.url?.takeIf(String::isNotBlank)
    }

    private fun openAttachment(attachment: FeedAttachment) {
        when (attachment.type) {
            RegexUtil.KEY_IMG -> onImagePreviewRequested?.invoke(attachment.url)
            RegexUtil.KEY_WEB -> onVideoPreviewRequested?.invoke(attachment.url)
            RegexUtil.KEY_YOU -> onYoutubeRequested?.invoke(attachment.url)
        }
    }

    override fun onViewRecycled() {
        attachments.value = emptyList()
        binding.widgetSlideHolder.visibility = GONE
        binding.widgetStatusComposeView.transitionName = null
        onImagePreviewRequested = null
        onVideoPreviewRequested = null
        onYoutubeRequested = null
    }
}
