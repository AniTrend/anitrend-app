package com.mxt.anitrend.initializer.analytics

import android.content.Context
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.initializer.contract.AbstractInitializer
import timber.log.Timber

class AnalyticsInitializer : AbstractInitializer<Unit>() {
    /**
     * Plants the analytics logging tree resolved from Koin, if the resolved
     * [ISupportAnalytics] implementation is a [Timber.Tree].
     *
     * Runs after [com.mxt.anitrend.initializer.injector.InjectorInitializer]
     * so Koin is guaranteed to be started before the lookup happens.
     */
    override fun create(context: Context) {
        val analyticsTree = koinOf<ISupportAnalytics>() as? Timber.Tree ?: return
        Timber.plant(analyticsTree)
    }
}
