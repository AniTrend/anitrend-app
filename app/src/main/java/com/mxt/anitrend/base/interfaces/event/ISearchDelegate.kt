package com.mxt.anitrend.base.interfaces.event

interface ISearchDelegate {
    fun onQueryChanged(query: String)
    fun onSearchSubmitted(query: String)
}
