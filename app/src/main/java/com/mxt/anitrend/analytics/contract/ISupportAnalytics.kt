package com.mxt.anitrend.analytics.contract

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

interface ISupportAnalytics {

    fun logCurrentScreen(context: FragmentActivity, tag: String)
    fun logCurrentState(tag: String, bundle: Bundle?)

    fun logException(throwable: Throwable)
    fun log(priority: Int = Log.VERBOSE, tag: String?, message: String)

    fun clearUserSession()
    fun setCrashAnalyticUser(userIdentifier: String)

    fun resetAnalyticsData()
}