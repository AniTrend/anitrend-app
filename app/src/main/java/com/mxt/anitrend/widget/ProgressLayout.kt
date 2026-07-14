package com.mxt.anitrend.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MainThread
import com.google.android.material.button.MaterialButton
import com.mxt.anitrend.R
import com.mxt.anitrend.widget.progress.ProgressLayoutState

class ProgressLayout
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private var state: ProgressLayoutState = ProgressLayoutState.initial()

    private val loadingView: View
    private val errorView: View
    private val errorIcon: ImageView
    private val errorMessage: TextView
    private val errorAction: MaterialButton
    private val initialContentVisibility = mutableMapOf<View, Int>()
    private val overlayIds = setOf(R.id.progressStateLoading, R.id.progressStateError)

    val isLoading: Boolean
        get() = state == ProgressLayoutState.LOADING

    val isContent: Boolean
        get() = state == ProgressLayoutState.CONTENT

    val isError: Boolean
        get() = state == ProgressLayoutState.ERROR

    init {
        val materialContext = ContextThemeWrapper(
            context,
            R.style.ThemeOverlay_AniTrend_ProgressLayout_Material3,
        )
        LayoutInflater.from(materialContext).inflate(R.layout.layout_progress_state_overlay, this, true)
        loadingView = findViewById(R.id.progressStateLoading)
        errorView = findViewById(R.id.progressStateError)
        errorIcon = findViewById(R.id.progressStateErrorIcon)
        errorMessage = findViewById(R.id.progressStateErrorText)
        errorAction = findViewById(R.id.progressStateErrorAction)
        showContent()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        captureContentVisibilityBaseline()
        applyState()
    }

    @MainThread
    fun showLoading() {
        if (!isMainThread()) {
            post { showLoading() }
            return
        }
        state = ProgressLayoutState.transition(state, ProgressLayoutState.LOADING)
        applyState()
    }

    @MainThread
    fun showContent() {
        if (!isMainThread()) {
            post { showContent() }
            return
        }
        state = ProgressLayoutState.transition(state, ProgressLayoutState.CONTENT)
        applyState()
    }

    @MainThread
    fun showError(
        drawable: Drawable?,
        message: CharSequence?,
        actionText: CharSequence?,
        action: OnClickListener?,
    ) {
        if (!isMainThread()) {
            post { showError(drawable, message, actionText, action) }
            return
        }
        state = ProgressLayoutState.transition(state, ProgressLayoutState.ERROR)
        errorIcon.setImageDrawable(drawable)
        errorIcon.visibility = if (drawable == null) View.GONE else View.VISIBLE
        errorMessage.text = message?.toString().orEmpty()

        if (action == null || actionText.isNullOrBlank()) {
            errorAction.visibility = View.GONE
            errorAction.text = ""
            errorAction.setOnClickListener(null)
        } else {
            errorAction.visibility = View.VISIBLE
            errorAction.text = actionText
            errorAction.setOnClickListener(action)
        }

        applyState()
    }

    fun showEmpty(
        drawable: Drawable?,
        message: CharSequence?,
    ) {
        showError(drawable = drawable, message = message, actionText = null, action = null)
    }

    @MainThread
    private fun applyState() {
        loadingView.visibility = if (state == ProgressLayoutState.LOADING) View.VISIBLE else View.GONE
        errorView.visibility = if (state == ProgressLayoutState.ERROR) View.VISIBLE else View.GONE

        forEachContentChild { child ->
            initialContentVisibility.putIfAbsent(child, child.visibility)
            child.visibility = when (state) {
                ProgressLayoutState.CONTENT -> initialContentVisibility[child] ?: View.VISIBLE
                ProgressLayoutState.LOADING,
                ProgressLayoutState.ERROR,
                -> View.GONE
            }
        }
    }

    private fun captureContentVisibilityBaseline() {
        forEachContentChild { child ->
            initialContentVisibility.putIfAbsent(child, child.visibility)
        }
    }

    private inline fun forEachContentChild(action: (View) -> Unit) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.id !in overlayIds) {
                action(child)
            }
        }
    }

    private fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()
}
