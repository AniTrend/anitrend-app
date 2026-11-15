package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.feed.StatusDeleteAction
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.NotifyUtil

data class StatusDeleteWidgetState(
    val isEnabled: Boolean,
    val isLoading: Boolean,
)

class StatusDeleteWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView {

    private var deleteListener: (() -> Unit)? = null
    private var recycled = false
    private var state by mutableStateOf(StatusDeleteWidgetState(isEnabled = true, isLoading = false))

    init {
        onInit()
    }

    override fun onInit() {
        val composeView =
            ComposeView(context).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
                setContent {
                    AniTrendTheme {
                        StatusDeleteAction(
                            isEnabled = state.isEnabled,
                            isLoading = state.isLoading,
                            onClick = ::handleDeleteClick,
                        )
                    }
                }
            }
        addView(
            composeView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ),
        )
    }

    fun render(state: StatusDeleteWidgetState) {
        recycled = false
        this.state = state
    }

    fun setOnDeleteListener(listener: (() -> Unit)?) {
        deleteListener = listener
    }

    override fun onViewRecycled() {
        recycled = true
        deleteListener = null
        state = StatusDeleteWidgetState(isEnabled = true, isLoading = false)
    }

    private fun handleDeleteClick() {
        if (recycled || !isAttachedToWindow) {
            return
        }
        if (state.isLoading) {
            NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            return
        }
        if (!state.isEnabled) {
            return
        }
        val listenerAtClick = deleteListener
        DialogUtil.createMessage(
            context,
            R.string.dialog_title_delete_activity,
            R.string.dialog_message_delete_activity,
        ) { _, _ ->
            if (!recycled && isAttachedToWindow && deleteListener === listenerAtClick) {
                listenerAtClick?.invoke()
            }
        }
    }
}
