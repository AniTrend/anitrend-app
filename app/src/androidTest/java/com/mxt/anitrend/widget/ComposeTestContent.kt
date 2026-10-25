package com.mxt.anitrend.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.test.platform.app.InstrumentationRegistry

/** Replaces the View fixture before composing, rather than using the empty-Activity test API. */
internal fun ProgressLayoutTestActivity.installComposeContent(content: @Composable () -> Unit) {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        setContentView(
            ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent(content)
            },
        )
    }
}
