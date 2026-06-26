package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.databinding.ActivitySplashBinding
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.view.activity.base.WelcomeActivity
import kotlinx.coroutines.delay

/**
 * Created by max on 2017/10/04.
 * Base splash screen
 */

class SplashActivity : ActivityBase<Nothing, BasePresenter>() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPresenter(BasePresenter(this))
        setViewModel(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.previewCredits.setImageResource(
            if (!CompatUtil.isLightTheme(presenter.settings)) {
                R.drawable.powered_by_giphy_light
            } else {
                R.drawable.powered_by_giphy_dark
            },
        )
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        lifecycleScope.launchWhenResumed {
            presenter.checkForUpdates(true)
            presenter.checkValidAuth()
            delay(500)
            makeRequest()
        }
    }

    override fun updateUI() {
        val freshInstall = presenter.settings.isFreshInstall
        val intent =
            Intent(
                this@SplashActivity,
                if (freshInstall) {
                    WelcomeActivity::class.java
                } else {
                    MainActivity::class.java
                },
            )
        startActivity(intent)
        finish()
    }

    override fun makeRequest() {
        if (presenter.checkIfMigrationIsNeeded()) {
            updateUI()
        } else {
            onMigrationFailed()
        }
    }

    private fun onMigrationFailed() {
        val drawable = getCompatTintedDrawable(R.drawable.ic_system_update_grey_600_24dp)
        val builder =
            DialogUtil
                .createDefaultDialog(this)
                .setTitle(R.string.title_migration_failed)
                .setMessage(R.string.text_migration_failed)
                .setPositiveButton(R.string.Ok) { d, _ ->
                    d.dismiss()
                    finish()
                }
        if (drawable != null) {
            builder.setIcon(drawable)
        }
        builder.show()
    }
}
