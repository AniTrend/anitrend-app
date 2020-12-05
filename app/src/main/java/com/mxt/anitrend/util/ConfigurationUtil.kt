package com.mxt.anitrend.util

import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.extension.applyConfiguredTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class ConfigurationUtil : KoinComponent {

    private val settings by inject<Settings>()

    @KeyUtil.ApplicationTheme
    private lateinit var currentTheme: String
    private lateinit var currentLocale: String

    /**
     * Applies appropriate theme and locale startup
     */
    fun onCreateAttach(base: ActivityBase<*, *>) {
        currentTheme = settings.theme
        currentLocale = settings.userLanguage ?: Locale.getDefault().language
        @StyleRes val theme = when (currentTheme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        base.setTheme(theme)
    }

    /**
     * Checks if the previously set theme is the same as the current when the activity resumes it's state
     */
    fun onResumeAttach(base: ActivityBase<*, *>) {
        if (currentTheme != settings.theme || currentLocale != settings.userLanguage) {
            with (base) {
                applyConfiguredTheme()
                val currentIntent = intent
                finish()
                overridePendingTransition(0, 0)
                startActivity(currentIntent)
                overridePendingTransition(0, 0)
            }
        }
    }

    /**
     * Applies correct night mode depending on the selected theme, however setting the night mode
     * off reset locale, possibly because there are no resource for string-night?
     */
    fun applyApplicationTheme() {
        if (CompatUtil.isLightTheme(settings))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}