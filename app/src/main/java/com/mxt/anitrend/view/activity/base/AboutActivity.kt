package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.detail.AboutFragment

class AboutActivity : AppCompatActivity() {

    private val toolbar by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Toolbar?>(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Preserve configured theme (previously handled by ActivityBase.configureActivity).
        val settings = KoinExt.get(Settings::class.java)
        val themeRes = when (settings.theme) {
            KeyUtil.THEME_DARK -> R.style.AppThemeDark
            KeyUtil.THEME_BLACK -> R.style.AppThemeBlack
            else -> R.style.AppThemeLight
        }
        setTheme(themeRes)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_frame_generic)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment = AboutFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, fragment, fragment.TAG)
            .commit()
    }
}
