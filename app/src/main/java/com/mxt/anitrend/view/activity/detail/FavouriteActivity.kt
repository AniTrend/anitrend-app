package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.detail.FavouritePageAdapter
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings

class FavouriteActivity : AppCompatActivity() {

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

        val binding = ActivityPagerGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pageAdapter =
            FavouritePageAdapter(this, applicationContext).apply {
                params = intent.extras ?: Bundle.EMPTY
            }
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = 3
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = pageAdapter.getPageTitle(position)
        }.attach()
    }
}
