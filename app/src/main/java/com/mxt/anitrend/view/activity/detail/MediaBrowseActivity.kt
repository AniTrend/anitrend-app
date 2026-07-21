package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.view.fragment.list.MediaBrowseFragment

class MediaBrowseActivity : AppCompatActivity() {

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

        val binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getStringExtra(KeyUtil.arg_activity_tag)?.let { tag ->
            supportActionBar?.title = MarkDownUtil.convert(this, tag)
        }

        val fragment = MediaBrowseFragment.newInstance(intent.extras ?: Bundle.EMPTY)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, fragment, fragment.TAG)
            .commit()
    }
}
