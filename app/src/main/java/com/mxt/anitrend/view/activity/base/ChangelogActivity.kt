package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.mxt.anitrend.R
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.fragment.detail.ChangelogFragment

/**
 * Activity that hosts the changelog screen as a full destination.
 * Used as an alternative to the changelog dialog when
 * [Settings.experimentalInitialScreens] is enabled.
 */
class ChangelogActivity : CommonActivity() {

    private val toolbar by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<Toolbar?>(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_frame_generic)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.text_what_is_new)
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, ChangelogFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
