package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.favourite.FavouriteToolbarAction
import com.mxt.anitrend.data.store.favourite.FavouriteFlag
import com.mxt.anitrend.util.NotifyUtil

/**
 * Immutable render state for the render-only [FavouriteToolbarWidget] path.
 *
 * The widget never mutates a model or an entity; callers re-render whenever the canonical
 * favourite store or the in-flight loading state changes.
 */
data class FavouriteWidgetRenderState(
    val isFavourite: Boolean,
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
) {
    companion object {
        /**
         * Maps a committed [FavouriteFlag] plus the in-flight loading flag onto a render state.
         *
         * Falls back to [fallbackIsFavourite] (for example the initially loaded entity flag)
         * while no committed store value exists yet, which keeps the icon stable during the
         * brief window before the store is seeded.
         */
        fun fromFlag(
            flag: FavouriteFlag?,
            fallbackIsFavourite: Boolean,
            isLoading: Boolean,
        ): FavouriteWidgetRenderState = FavouriteWidgetRenderState(
            isFavourite = flag?.isFavourite ?: fallbackIsFavourite,
            isEnabled = true,
            isLoading = isLoading,
        )
    }
}

/**
 * Created by max on 2018/01/31.
 * Widget for handling favourite toggles
 */
class FavouriteToolbarWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView {

    private lateinit var composeView: ComposeView
    private val composeState = mutableStateOf(FavouriteWidgetRenderState(isFavourite = false))
    private var toggleAction: (() -> Unit)? = null
    private var renderState: FavouriteWidgetRenderState? = null
    private var shouldShowContent = false
    private var compositionInstalled = false

    /**
     * Fire-and-forget action delivery for the render-only path. The widget does not await a
     * result; callers observe the canonical store and re-render on the committed response.
     */
    fun setOnToggleAction(action: (() -> Unit)?) {
        toggleAction = action
    }

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
        composeView =
            ComposeView(context).apply {
                id = View.generateViewId()
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
                )
                visibility = GONE
            }
        addView(
            composeView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER,
            ),
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (shouldShowContent) showAfterAttach()
    }

    override fun onDetachedFromWindow() {
        composeView.visibility = GONE
        super.onDetachedFromWindow()
    }

    private fun showAfterAttach() {
        composeView.post {
            if (shouldShowContent && composeView.isAttachedToWindow) {
                if (!compositionInstalled) {
                    composeView.setContent {
                        AniTrendTheme {
                            FavouriteToolbarAction(
                                isFavourite = composeState.value.isFavourite,
                                isEnabled = composeState.value.isEnabled,
                                isLoading = composeState.value.isLoading,
                                onClick = ::handleActionClick,
                            )
                        }
                    }
                    compositionInstalled = true
                }
                composeView.visibility = VISIBLE
            }
        }
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        toggleAction = null
        renderState = null
        if (composeState.value.isLoading) {
            composeState.value = composeState.value.copy(isLoading = false)
        }
    }

    /**
     * Render-only path used by toolbar owners. Loading and favorite visuals are driven by the
     * immutable state; committed mutations remain owned by the caller's canonical store.
     */
    fun render(state: FavouriteWidgetRenderState) {
        renderState = state
        composeState.value = state
        shouldShowContent = true
        if (composeView.isAttachedToWindow) showAfterAttach()
    }

    private fun handleActionClick() {
        val state = renderState
        if (state == null) {
            NotifyUtil.makeText(context, R.string.text_activity_loading, Toast.LENGTH_SHORT).show()
            return
        }
        when {
            state.isLoading -> NotifyUtil.makeText(context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
            state.isEnabled -> toggleAction?.invoke()
        }
    }
}
