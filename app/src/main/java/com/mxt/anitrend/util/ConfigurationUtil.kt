package com.mxt.anitrend.util

import androidx.annotation.StyleRes
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.extension.applyConfiguredTheme
import org.koin.core.KoinComponent
import org.koin.core.inject

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
        currentLocale = settings.userLanguage
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
}