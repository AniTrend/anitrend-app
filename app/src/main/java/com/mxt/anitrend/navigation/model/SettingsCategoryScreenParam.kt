package com.mxt.anitrend.navigation.model

import kotlinx.parcelize.Parcelize

/**
 * Navigation parameter for the legacy settings category destination.
 *
 * @property categoryId Stable settings category id resolved by the destination.
 */
@Parcelize
data class SettingsCategoryScreenParam(
    val categoryId: String,
) : ScreenParam
