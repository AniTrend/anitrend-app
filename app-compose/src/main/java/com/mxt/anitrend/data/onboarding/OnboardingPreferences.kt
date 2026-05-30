package com.mxt.anitrend.data.onboarding

import android.content.Context
import android.content.SharedPreferences

class OnboardingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    val hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_COMPLETED, false)

    fun markCompleted() {
        prefs.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    companion object {
        private const val KEY_COMPLETED = "onboarding_completed"
    }
}
