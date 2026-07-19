package com.mxt.anitrend.base.interfaces.event

/**
 * Delegate interface for handling search events across activities and bottom sheets.
 * Implementations receive query changes and submission events without the base
 * needing to know about the underlying search view component.
 */
interface ISearchDelegate {
    fun onQueryChanged(query: String?)
    fun onSearchSubmitted(query: String?)
    fun onSearchShown() {}
    fun onSearchClosed() {}

    /**
     * Returns true if search is currently active/open.
     * Used by host classes for lifecycle decisions (e.g. back press).
     */
    fun isSearchActive(): Boolean = false

    /**
     * Requests the search UI to dismiss/close.
     * Returns true if the delegate handled the dismissal.
     */
    fun dismissSearch(): Boolean = false
}
