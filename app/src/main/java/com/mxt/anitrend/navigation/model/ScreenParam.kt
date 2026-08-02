package com.mxt.anitrend.navigation.model

import android.os.Parcelable

/**
 * Marker contract for typed, parcelable navigation parameters.
 *
 * Screen parameters carry only stable identity and the minimal reconstruction state a
 * destination needs to resolve its current domain state. Domain entities, persistence
 * entities, and remote models must not be used as navigation parameters.
 */
interface ScreenParam : Parcelable
