package com.mxt.anitrend.util.locale

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import com.mxt.anitrend.util.Settings
import java.util.*

/**
 * This class is used to change the application locale.
 *
 * Created by gunhansancar on 07/10/15.
 */
object LocaleUtil {

    @Deprecated(
        message = "We have migrated to androidx.appcompat with multi-locale support",
        replaceWith = ReplaceWith(""),
        level = DeprecationLevel.WARNING
    )
    fun onAttach(context: Context, settings: Settings): Context =
        applyConfiguration(context, settings)

    fun applyConfiguration(context: Context, settings: Settings): Context {
        val locale = scopeLocale(settings)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else updateResourcesLegacy(context, locale)
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

    fun scopeLocale(settings: Settings): Locale {
        return settings.userLanguage?.let { language ->
            Locale(language)
        } ?: Locale.getDefault()
    }
}