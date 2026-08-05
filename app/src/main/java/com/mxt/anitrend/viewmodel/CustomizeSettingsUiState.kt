package com.mxt.anitrend.viewmodel

/**
 * Immutable state of the Customize settings category.
 *
 * Values are the raw shared preference values (stable identity); the
 * fragments resolve the display labels from resources.
 */
data class CustomizeSettingsUiState(
    val theme: String,
    val language: String,
    val listViewStyle: String,
)
