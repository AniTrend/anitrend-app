package com.mxt.anitrend.view.activity.index

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.databinding.ActivitySplashBinding
import com.mxt.anitrend.extension.getCompatTintedDrawable
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.migration.MigrationUtil
import com.mxt.anitrend.util.migration.Migrations
import com.mxt.anitrend.view.activity.CommonActivity
import com.mxt.anitrend.view.activity.base.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Created by max on 2017/10/04.
 * Base splash screen
 */

class SplashActivity : CommonActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val userRepository: UserRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.previewCredits.setImageResource(
            if (!CompatUtil.isLightTheme(settings)) {
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
    private fun onActivityReady() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                checkForUpdates(true)
                checkValidAuth()
                delay(500)
                makeRequest()
            }
        }
    }

    private fun updateUI() {
        val freshInstall = settings.isFreshInstall
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

    private fun makeRequest() {
        if (checkIfMigrationIsNeeded()) {
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

    private fun checkForUpdates(silent: Boolean) {
        val scheduler = JobSchedulerUtil(settings)
        scheduler.startUpdateJob(applicationContext, silent)
    }

    private fun checkValidAuth() {
        if (settings.isAuthenticated) {
            if (userRepository.cachedCurrentUser == null) {
                WebTokenRequest.invalidateInstance(applicationContext)
            }
        }
    }

    private fun checkIfMigrationIsNeeded(): Boolean {
        if (!settings.isFreshInstall) {
            val migrationUtil = MigrationUtil.Builder()
                .addMigration(Migrations.MIGRATION_101_108)
                .addMigration(Migrations.MIGRATION_109_134)
                .addMigration(Migrations.MIGRATION_135_136)
                .addMigration(Migrations.MIGRATION_18400_18500)
                .addMigration(Migrations.MIGRATION_1090700_1090800)
                .build(applicationContext)
            return migrationUtil.applyMigration()
        }
        return true
    }
}
