package com.mxt.anitrend.initializer.injector

import android.content.Context
import androidx.startup.Initializer
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.extension.workManagerFactory
import com.mxt.anitrend.initializer.ApplicationInitializer
import com.mxt.anitrend.koin.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.KOIN_TAG
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import timber.log.Timber

class InjectorInitializer : Initializer<Unit> {

    /**
     * Initializes and a component given the application [Context]
     *
     * @param context The application context.
     */
    override fun create(context: Context) {
        startKoin {
            androidContext(context)
            logger(KoinLogger())
            workManagerFactory()
            modules(appModules)
        }
    }

    /**
     * @return A list of dependencies that this [Initializer] depends on. This is
     * used to determine initialization order of [Initializer]s.
     *
     * For e.g. if a [Initializer] `B` defines another [Initializer] `A` as its dependency,
     * then `A` gets initialized before `B`.
     */
    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(ApplicationInitializer::class.java)

    internal class KoinLogger(
        logLevel: Level = if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR
    ) : Logger(logLevel) {
        override fun log(level: Level, msg: MESSAGE) {
            when (level) {
                Level.DEBUG -> Timber.tag(KOIN_TAG).v(msg)
                Level.INFO -> Timber.tag(KOIN_TAG).i(msg)
                Level.ERROR -> Timber.tag(KOIN_TAG).e(msg)
                Level.NONE -> { /* logging disabled */ }
            }
        }
    }
}