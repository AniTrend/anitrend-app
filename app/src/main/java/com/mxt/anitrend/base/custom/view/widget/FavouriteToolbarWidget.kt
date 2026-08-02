package com.mxt.anitrend.base.custom.view.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.data.store.favourite.FavouriteFlag
import com.mxt.anitrend.databinding.WidgetToolbarFavouriteBinding
import com.mxt.anitrend.extension.getCompatDrawable
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.extension.getLayoutInflater
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
    CustomView,
    View.OnClickListener {

    private lateinit var binding: WidgetToolbarFavouriteBinding

    private val tagName = FavouriteToolbarWidget::class.java.simpleName
    private var toggleAction: (() -> Unit)? = null
    private var renderState: FavouriteWidgetRenderState? = null

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
        binding = WidgetToolbarFavouriteBinding.inflate(context.getLayoutInflater(), this, true)
        binding.widgetFlipper.setOnClickListener(this)
    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {
        toggleAction = null
        renderState = null
        resetFlipperState()
    }

    private fun resetFlipperState() {
        if (binding.widgetFlipper.displayedChild == LOADING_STATE) {
            binding.widgetFlipper.displayedChild = CONTENT_STATE
        }
    }

    /**
     * Render-only path used by migrated favourite slices. Accepts an immutable
     * [FavouriteWidgetRenderState]; the widget never mutates a model. Loading is state driven,
     * so any render with [FavouriteWidgetRenderState.isLoading] false resets the flipper back
     * to the content child (bounded loading reset), even after a failure or a re-render.
     */
    fun render(state: FavouriteWidgetRenderState) {
        renderState = state
        binding.widgetFlipper.visibility = VISIBLE
        binding.widgetFlipper.displayedChild = if (state.isLoading) LOADING_STATE else CONTENT_STATE
        binding.widgetLike.setImageDrawable(
            favouriteDrawable(isFavourite = state.isFavourite, requiresTint = true),
        )
    }

    override fun onClick(view: View) {
        if (view.id == R.id.widget_flipper) {
            val state = renderState
            if (state == null) {
                NotifyUtil
                    .makeText(
                        context,
                        R.string.text_activity_loading,
                        Toast.LENGTH_SHORT,
                    ).show()
                return
            }
            when {
                state.isLoading -> {
                    NotifyUtil
                        .makeText(
                            context,
                            R.string.busy_please_wait,
                            Toast.LENGTH_SHORT,
                        ).show()
                }
                state.isEnabled -> toggleAction?.invoke()
            }
        }
    }

    private fun favouriteDrawable(
        isFavourite: Boolean,
        requiresTint: Boolean,
    ): Drawable? = when {
        isFavourite && requiresTint ->
            context.getCompatTintedDrawable(R.drawable.ic_favorite_white_24dp)
        isFavourite ->
            context.getCompatDrawable(R.drawable.ic_favorite_white_24dp)
        requiresTint ->
            context.getCompatTintedDrawable(R.drawable.ic_favorite_border_white_24dp)
        else ->
            context.getCompatDrawable(R.drawable.ic_favorite_border_white_24dp)
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1
    }
}
