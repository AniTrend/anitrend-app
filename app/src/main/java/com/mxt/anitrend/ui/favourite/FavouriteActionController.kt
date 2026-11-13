package com.mxt.anitrend.ui.favourite

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.util.NotifyUtil

internal class FavouriteActionController(
    private val composeView: ComposeView,
) {
    private val state = mutableStateOf(FavouriteActionState(count = 0, isLiked = false, isEnabled = true, isLoading = false))
    private var toggleAction: (() -> Unit)? = null
    private var recycled = true

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
        )
        composeView.setContent {
            AniTrendTheme {
                FavouriteAction(
                    state = state.value,
                    onClick = ::onClick,
                )
            }
        }
    }

    fun render(
        state: FavouriteActionState,
        toggleAction: () -> Unit,
    ) {
        recycled = false
        this.toggleAction = toggleAction
        this.state.value = state
    }

    fun onViewRecycled() {
        recycled = true
        toggleAction = null
        state.value = FavouriteActionState(count = 0, isLiked = false, isEnabled = true, isLoading = false)
    }

    private fun onClick() {
        if (recycled || !composeView.isAttachedToWindow) {
            return
        }
        if (state.value.isLoading) {
            NotifyUtil.makeText(composeView.context, R.string.busy_please_wait, Toast.LENGTH_SHORT).show()
        } else if (state.value.isEnabled) {
            toggleAction?.invoke()
        }
    }
}
