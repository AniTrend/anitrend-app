package com.mxt.anitrend.ui.settings

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.mxt.anitrend.ui.designsystem.AniTrendTheme

/** Adds lifecycle-safe Compose content to a settings Fragment host. */
internal fun LinearLayout.addSettingsComposeContent(content: @Composable () -> Unit) {
    addView(
        ComposeView(context).apply {
            id = View.generateViewId()
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                AniTrendTheme {
                    content()
                }
            }
        },
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ),
    )
}
