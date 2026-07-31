package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetDeleteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getLayoutInflater
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
    CustomView,
    View.OnClickListener {

    private lateinit var binding: WidgetDeleteBinding
    private var deleteListener: (() -> Unit)? = null
    private var recycled = false
    private var state = StatusDeleteWidgetState(isEnabled = true, isLoading = false)

    init {
        onInit()
    }

    override fun onInit() {
        binding = WidgetDeleteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetDelete.setCompoundDrawablesWithIntrinsicBounds(
            context.getCompatDrawable(R.drawable.ic_delete_red_600_18dp),
            null,
            null,
            null,
        )
        binding.widgetFlipper.setOnClickListener(this)
        render(state)
    }

    fun render(state: StatusDeleteWidgetState) {
        recycled = false
        this.state = state
        binding.widgetFlipper.displayedChild = if (state.isLoading) LOADING_STATE else CONTENT_STATE
        binding.widgetFlipper.isEnabled = state.isEnabled
        binding.widgetDelete.isEnabled = state.isEnabled
    }

    fun setOnDeleteListener(listener: (() -> Unit)?) {
        deleteListener = listener
    }

    override fun onViewRecycled() {
        recycled = true
        deleteListener = null
        state = StatusDeleteWidgetState(isEnabled = true, isLoading = false)
        render(state)
    }

    override fun onClick(view: View) {
        if (view.id != R.id.widget_flipper || recycled || !isAttachedToWindow) {
            return
        }
        if (state.isLoading) {
            NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            return
        }
        if (!state.isEnabled) {
            return
        }
        DialogUtil.createMessage(
            context,
            R.string.dialog_title_delete_activity,
            R.string.dialog_message_delete_activity,
        ) { _, _ ->
            deleteListener?.invoke()
        }
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
