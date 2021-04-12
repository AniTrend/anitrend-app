package com.mxt.anitrend.initializer

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.startup.Initializer
import com.google.android.gms.security.ProviderInstaller
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.initializer.logger.TimberInitializer
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class ApplicationInitializer : Initializer<Unit> {

    /**
     * Initializes and a component given the application [Context]
     *
     * @param context The application context.
     */
    override fun create(context: Context) {
        runCatching {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                val installerListener = object : ProviderInstaller.ProviderInstallListener {
                    override fun onProviderInstalled() {
                        Timber.i("Provider installed successfully")
                    }

                    override fun onProviderInstallFailed(code: Int, intent: Intent) {
                        Timber.e("Provider installer failed to patch device -> code: $code, intent: $intent")
                    }
                }

                ProviderInstaller.installIfNeededAsync(
                    context, installerListener
                )
            }
        }.exceptionOrNull()?.printStackTrace()

        EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG)
            .sendNoSubscriberEvent(BuildConfig.DEBUG)
            .sendSubscriberExceptionEvent(BuildConfig.DEBUG)
            .throwSubscriberException(BuildConfig.DEBUG)
            .installDefaultEventBus()
    }

    /**
     * @return A list of dependencies that this [Initializer] depends on. This is
     * used to determine initialization order of [Initializer]s.
     * <br></br>
     * For e.g. if a [Initializer] `B` defines another
     * [Initializer] `A` as its dependency, then `A` gets initialized before `B`.
     */
    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(TimberInitializer::class.java)
}