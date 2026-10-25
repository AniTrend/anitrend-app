package com.mxt.anitrend.extension

import androidx.fragment.app.FragmentActivity

/**
 * Re-themes without a restart: [applyConfiguredTheme] recreates the host
 * activity so the newly selected theme inflates from scratch.
 */
fun FragmentActivity.applyConfiguredTheme() {
    runCatching {
        recreate()
    }.exceptionOrNull()?.printStackTrace()
}
