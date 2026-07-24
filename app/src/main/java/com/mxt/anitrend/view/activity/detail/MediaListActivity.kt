package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.mxt.anitrend.R
import com.mxt.anitrend.adapter.pager.index.MediaListPageAdapter
import com.mxt.anitrend.databinding.ActivityPagerGenericBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings

class MediaListActivity : AppCompatActivity() {

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

        val bundle = intent.extras
        val mediaType = bundle?.getString(KeyUtil.arg_mediaType)
        bundle?.let {
            setTitle(
                if (CompatUtil.equals(mediaType, KeyUtil.ANIME)) {
                    R.string.title_anime_list
                } else {
                    R.string.title_manga_list
                },
            )
        }

        val pageAdapter =
            MediaListPageAdapter(this, applicationContext).apply {
                params = bundle ?: Bundle.EMPTY
            }
        binding.contentMain.pageContainer.adapter = pageAdapter
        binding.contentMain.pageContainer.offscreenPageLimit = 5
        TabLayoutMediator(binding.customTab.smartTab, binding.contentMain.pageContainer) { tab, position ->
            tab.text = pageAdapter.getPageTitle(position)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.action_settings).isVisible = false
        menu.findItem(R.id.action_extra).isVisible = false
        menu.findItem(R.id.action_share).isVisible = false
        return super.onCreateOptionsMenu(menu)
    }
}
