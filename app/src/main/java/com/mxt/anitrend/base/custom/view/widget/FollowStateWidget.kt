package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Handler
import android.os.Looper
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

/**
 * Created by max on 2017/11/16.
 * widget that represents the state of an
 * external user, either following or not
 *
 * The widget renders a user ID and committed follow flag, then emits the affected
 * userId to its listener. It never retains the target entity, resolves repositories,
 * or starts business coroutines. The owning screen pushes committed display values
 * back through [setUserState].
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

    fun interface Listener {
        fun onToggleFollow(userId: Long)
    }

    private var boundUserId = 0L
    private var isFollowing = false
    private lateinit var binding: WidgetButtonStateBinding
    private var listener: Listener? = null
    private var hasBoundUser = false
    private var currentUser: UserBase? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Bounded fallback so a fire-and-forget mutation can never leave the widget
     * permanently in the loading state (for example when the mutation fails and the
     * screen has nothing new to render).
     */
    private val loadingFallback = Runnable {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    fun setCurrentUser(user: UserBase?) {
        this.currentUser = user
        // Authentication context may arrive after the row's display state.
        if (hasBoundUser) {
            applyVisibilityState()
        }
    }

    init {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        binding = WidgetButtonStateBinding.inflate(context.getLayoutInflater(), this, true)
        // The inner button state text is the actual tap target Android delivers
        // touches (and accessibility activations) to, so it must carry the same
        // listener as the flipper. The CONTENT_STATE guard in [onClick] makes a
        // single tap dispatch at most one mutation even if both paths ever fire.
        binding.widgetFlipper.setOnClickListener(this)
        binding.buttonStateText.setOnClickListener(this)
    }

    /** Binds committed display values without retaining a mutable target entity. */
    fun setUserState(userId: Long, isFollowing: Boolean) {
        boundUserId = userId
        this.isFollowing = isFollowing
        hasBoundUser = true
        resetLoadingState()
        applyVisibilityState()
    }

    /**
     * Decides whether the follow control is rendered for the bound user:
     * hidden when there is no authenticated current user context or when the row
     * is the current user themself, visible otherwise.
     */
    private fun applyVisibilityState() {
        if (!hasBoundUser) return
        if (currentUser != null) {
            if (boundUserId == 0L || currentUser?.id != boundUserId) {
                visibility = VISIBLE
                setControlText()
            } else {
                visibility = GONE
            }
        } else {
            visibility = GONE
        }
    }

    private fun setControlText() {
        if (isFollowing) {
            binding.buttonStateText.setText(R.string.following)
        } else {
            binding.buttonStateText.setText(R.string.follow)
        }
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        hasBoundUser = false
        listener = null
        visibility = VISIBLE
        resetLoadingState()
    }

    private fun resetLoadingState() {
        mainHandler.removeCallbacks(loadingFallback)
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    private fun showLoadingState() {
        binding.widgetFlipper.displayedChild = LOADING_STATE
        mainHandler.postDelayed(loadingFallback, LOADING_FALLBACK_TIMEOUT_MS)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.widget_flipper,
            R.id.button_state_text,
            -> {
                if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                    if (!hasBoundUser) {
                        resetLoadingState()
                        return
                    }
                    val configuredListener = listener ?: return
                    showLoadingState()
                    configuredListener.onToggleFollow(boundUserId)
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

        const val LOADING_FALLBACK_TIMEOUT_MS = 10_000L
    }
}
