package com.mxt.anitrend.base.custom.view.text

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.model.entity.anilist.FeedList
import java.util.Locale

/**
 * Created by max on 2017/11/13.
 * Feeds including progress & status
 */
class FeedHeadlineTextView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr),
    CustomView {
    init {
        onInit()
    }

    override fun onInit() {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.heading_text_size).toFloat())
        ellipsize = TextUtils.TruncateAt.END
        maxLines = 2
    }

    override fun onViewRecycled() = Unit

    companion object {
        @JvmStatic
        fun setHeadline(
            headline: FeedHeadlineTextView,
            model: FeedList,
        ) {
            val title =
                model.media
                    ?.title
                    ?.romaji
                    .orEmpty()
            if (model.text.isNullOrEmpty()) {
                headline.text =
                    String.format(
                        Locale.getDefault(),
                        "%s: %s",
                        model.status,
                        title,
                    )
            } else {
                headline.text =
                    String.format(
                        Locale.getDefault(),
                        "%s %s of: %s",
                        model.status,
                        model.text,
                        title,
                    )
            }
        }
    }
}
