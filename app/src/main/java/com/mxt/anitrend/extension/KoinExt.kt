package com.mxt.anitrend.extension

import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkManager
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.worker.factory.WorkManagerFactory
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

object KoinExt : KoinComponent {

    /**
     * Helper to retrieve dependencies by class definition
     *
     * @param `class` registered class in koin modules
     */
    @JvmStatic
    fun <T : Any> get(`class`: Class<T>): T {
        return getKoin().get(`class`.kotlin, null, null)
    }
}

/**
 * Helper to resolve koin dependencies
 *
 * @param qualifier Help qualify a component
 * @param parameters Help define a DefinitionParameters
 *
 * @return [T]
 */
inline fun <reified T> koinOf(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    val koin = GlobalContext.get()
    return koin.get(qualifier, parameters)
}

private fun KoinApplication.createWorkManagerFactory() {
    val factory = DelegatingWorkerFactory()
    with (factory) {
        addFactory(WorkManagerFactory())
    }

    val configuration = Configuration.Builder()
        .setWorkerFactory(factory)

    if (BuildConfig.DEBUG)
        configuration.setMinimumLoggingLevel(Log.VERBOSE)
    else configuration.setMinimumLoggingLevel(Log.WARN)

    WorkManager.initialize(koin.get(), configuration.build())
}

/**
 * Setup the KoinWorkerFactory instance
 *
 * @see org.koin.androidx.workmanager.koin.workManagerFactory
 */
internal fun KoinApplication.workManagerFactory() {
    createWorkManagerFactory()
}