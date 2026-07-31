package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetAutoIncrementerBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import java.util.Locale

data class AutoIncrementWidgetState(
    val progress: Int,
    val maxProgress: Int,
    val isEnabled: Boolean,
    val isLoading: Boolean,
    val status: String?,
    val mediaType: String?,
)

/**
 * Created by max on 2018/02/22.
 * auto increment widget for changing series progress with just a tap
 */
class AutoIncrementWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    private lateinit var binding: WidgetAutoIncrementerBinding
    private var state: AutoIncrementWidgetState? = null
    private var incrementListener: (() -> Unit)? = null

    fun setOnIncrementListener(listener: (() -> Unit)?) {
        incrementListener = listener
    }

    init {
        onInit()
    }

    override fun onInit() {
        binding = WidgetAutoIncrementerBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val currentState = state ?: return
        if (view.id != R.id.widget_flipper) {
            return
        }
        if (currentState.isLoading || binding.widgetFlipper.displayedChild == LOADING_STATE) {
            NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            return
        }
        if (currentState.isEnabled) {
            incrementListener?.invoke()
            return
        }

        if (isIncrementLimitReached(currentState)) {
            NotifyUtil
                .makeText(
                    context,
                    if (currentState.mediaType == KeyUtil.ANIME) {
                        R.string.text_unable_to_increment_episodes
                    } else {
                        R.string.text_unable_to_increment_chapters
                    },
                    R.drawable.ic_warning_white_18dp,
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun render(state: AutoIncrementWidgetState) {
        this.state = state
        binding.widgetFlipper.displayedChild = if (state.isLoading) LOADING_STATE else CONTENT_STATE
        binding.widgetFlipper.isEnabled = true
        binding.seriesProgressIncrement.text = buildProgressLabel(state)
    }

    override fun onViewRecycled() {
        incrementListener = null
        state = null
        resetFlipperState()
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    private fun buildProgressLabel(state: AutoIncrementWidgetState): String {
        if (state.status == KeyUtil.NOT_YET_RELEASED) {
            return context.getString(R.string.TBA)
        }

        val total: String = if (state.maxProgress > 0) {
            state.maxProgress.toString()
        } else {
            "?"
        }
        val showIncrementAffordance = !isIncrementLimitReached(state)
        return if (showIncrementAffordance) {
            String.format(Locale.getDefault(), "%s/%s +", state.progress, total)
        } else {
            String.format(Locale.getDefault(), "%s/%s", state.progress, total)
        }
    }

    private fun isIncrementLimitReached(state: AutoIncrementWidgetState): Boolean = state.maxProgress > 0 && state.progress >= state.maxProgress

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
