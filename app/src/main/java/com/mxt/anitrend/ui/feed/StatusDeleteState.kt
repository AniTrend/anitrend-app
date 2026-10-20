package com.mxt.anitrend.ui.feed

/**
 * Render state for the feed delete control: the owning screen pushes the
 * committed state, the row renders it without owning the mutation.
 */
data class StatusDeleteWidgetState(
    val isEnabled: Boolean,
    val isLoading: Boolean,
)
