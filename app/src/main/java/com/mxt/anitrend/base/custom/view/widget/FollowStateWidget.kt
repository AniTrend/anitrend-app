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
 * The widget is render-only with respect to follow mutations: it renders the bound
 * [UserBase] as a transitional read-only source and emits the affected userId to its
 * listener fire-and-forget. It never resolves repositories and never starts business
 * coroutines; the owning screen owns the mutation lifecycle and pushes committed state
 * back through [setUserModel].
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

    private var model: UserBase? = null
    private lateinit var binding: WidgetButtonStateBinding
    private var listener: Listener? = null
    private var recycled = false
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
        // A newly bound state always reflects the latest committed render source,
        // so a pending mutation loading state is reset.
        resetLoadingState()
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
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        recycled = true
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
            R.id.widget_flipper -> {
                if (binding.widgetFlipper.displayedChild == CONTENT_STATE) {
                    val currentModel = model ?: run {
                        resetLoadingState()
                        return
                    }
                    showLoadingState()
                    listener?.onToggleFollow(currentModel.id)
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
