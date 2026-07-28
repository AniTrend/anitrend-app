package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetButtonStateBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.util.NotifyUtil
import timber.log.Timber

/**
 * Created by max on 2017/11/16.
 * widget that represents the state of an
 * external user, either following or not
 */
class FollowStateWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener {

    interface Listener {
        fun onToggleFollow(
            userId: Long,
            onResult: (Result<UserBase>) -> Unit,
        )
    }

    private var model: UserBase? = null
    private lateinit var binding: WidgetButtonStateBinding
    private val tagName = FollowStateWidget::class.java.simpleName
    private var listener: Listener? = null
    private var recycled = false
    private var currentUser: UserBase? = null

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setCurrentUser(user: UserBase?) {
        this.currentUser = user
    }

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetButtonStateBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    fun setUserModel(model: UserBase) {
        recycled = false
        this.model = model
        if (currentUser != null) {
            if (!isCurrentUser(model)) {
                setControlText()
            } else {
                visibility = GONE
            }
        } else {
            visibility = GONE
        }
    }

    private fun isCurrentUser(userBase: UserBase): Boolean = userBase.id != 0L && currentUser?.id == userBase.id

    private fun setControlText() {
        val currentModel = model ?: return
        if (currentModel.isFollowing) {
            binding.buttonStateText.setText(R.string.following)
        } else {
            binding.buttonStateText.setText(R.string.follow)
        }
        resetFlipperState()
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        recycled = true
        listener = null
        visibility = VISIBLE
        resetFlipperState()
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.widget_flipper -> {
                if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                    binding.widgetFlipper.showNext()
                    val currentModel = model ?: run {
                        resetFlipperState()
                        return
                    }
                    listener?.onToggleFollow(currentModel.id) { result ->
                        if (recycled || !isAttachedToWindow) return@onToggleFollow
                        result.onSuccess {
                            model?.toggleFollow()
                            setControlText()
                        }.onFailure { throwable ->
                            Timber.e(throwable)
                            setControlText()
                        }
                    }
                } else {
                    NotifyUtil
                        .makeText(
                            context,
                            R.string.busy_please_wait,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
