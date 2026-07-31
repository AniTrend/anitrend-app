package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetFavouriteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.NotifyUtil
import java.util.Locale

data class FavouriteWidgetState(
    val count: Int,
    val isLiked: Boolean,
    val isEnabled: Boolean,
    val isLoading: Boolean,
)

class FavouriteWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    private lateinit var binding: WidgetFavouriteBinding
    private var toggleListener: (() -> Unit)? = null
    private var recycled = false
    private var state = FavouriteWidgetState(count = 0, isLiked = false, isEnabled = true, isLoading = false)

    init {
        onInit()
    }

    override fun onInit() {
        binding = WidgetFavouriteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
        render(state)
    }

    override fun onViewRecycled() {
        recycled = true
        toggleListener = null
        state = FavouriteWidgetState(count = 0, isLiked = false, isEnabled = true, isLoading = false)
        render(state)
    }

    fun render(state: FavouriteWidgetState) {
        recycled = false
        this.state = state
        binding.widgetFlipper.displayedChild = if (state.isLoading) LOADING_STATE else CONTENT_STATE
        binding.widgetFlipper.isEnabled = state.isEnabled
        binding.widgetLike.isEnabled = state.isEnabled
        binding.widgetLike.setCompoundDrawablesWithIntrinsicBounds(
            if (state.isLiked) {
                context.getCompatDrawable(R.drawable.ic_favorite_grey_600_18dp, R.color.colorStateRed)
            } else {
                context.getCompatDrawable(R.drawable.ic_favorite_grey_600_18dp)
            },
            null,
            null,
            null,
        )
        binding.widgetLike.text = convertToText(state.count)
    }

    fun setOnToggleListener(listener: (() -> Unit)?) {
        toggleListener = listener
    }

    override fun onClick(view: View) {
        if (view.id != R.id.widget_flipper || recycled || !isAttachedToWindow) {
            return
        }
        when {
            state.isLoading -> {
                NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            }
            state.isEnabled -> {
                toggleListener?.invoke()
            }
        }
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1

        fun convertToText(count: Int): String = String.format(Locale.getDefault(), " %d ", count)
    }
}
