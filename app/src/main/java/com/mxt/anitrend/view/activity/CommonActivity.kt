package com.mxt.anitrend.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.applyConfiguredTheme
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.fragment.android.setupKoinFragmentFactory
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope
import java.util.Locale

abstract class CommonActivity :
    AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScope()
    protected val settings by inject<Settings>()

    protected var currentLocale: String? = null
    protected var currentTheme: String? = null

    @Suppress("DEPRECATION")
    private fun onResumeThemeCheck() {
        if (currentTheme != settings.theme || currentLocale != settings.userLanguage) {
            applyConfiguredTheme()
            val currentIntent = intent
            finish()
            overridePendingTransition(0, 0)
            startActivity(currentIntent)
            overridePendingTransition(0, 0)
        }
    }

    protected fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (CompatUtil.isLightTheme(settings)) {
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
                true
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars =
                true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentLocale = settings.userLanguage ?: Locale.getDefault().language
        currentTheme = settings.theme
        val themeRes = when (currentTheme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
        enableEdgeToEdge()
        setupKoinFragmentFactory()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        onResumeThemeCheck()
    }
}
