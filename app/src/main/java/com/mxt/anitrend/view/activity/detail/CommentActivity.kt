package com.mxt.anitrend.view.activity.detail

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.ActivityFrameGenericBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.IntentBundleUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.fragment.detail.CommentFragment

class CommentActivity : AppCompatActivity() {

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

        // Process deep links (e.g. anilist.co/activity/{id}) so arg extras are
        // injected before the fragment reads them.
        IntentBundleUtil(intent).checkIntentData(this)

        val binding = ActivityFrameGenericBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.customToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireNotNull(intent.extras?.takeIf { it.containsKey(KeyUtil.arg_id) }) {
            "CommentActivity requires ${KeyUtil.arg_id}"
        }

        val fragment = CommentFragment.newInstance(intent.extras ?: Bundle.EMPTY)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_frame, fragment, fragment.TAG)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
