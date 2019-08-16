package com.mxt.anitrend.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.*

/**
 * This class is used to change the application locale.
 *
 * Created by gunhansancar on 07/10/15.
 */
object LocaleUtil {

    fun onAttach(context: Context): Context {
        val language = Settings(context).userLanguage ?: Locale.getDefault().language

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, Locale(language))
        } else updateResourcesLegacy(context, Locale(language))

    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }
}