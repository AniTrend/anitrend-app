package com.mxt.anitrend.view.activity.base

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.SettingsActivityBinding
import com.mxt.anitrend.view.activity.CommonActivity

/**
 * Settings entry point hosting the Navigation 2 (Fragment/XML) settings
 * graph. The toolbar and theme/locale shell behavior come from
 * [CommonActivity]; the up button pops the settings back stack and finishes
 * the activity when the hub is the current destination.
 */
class SettingsActivity : CommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.settings) as? NavHostFragment
            ?: return super.onSupportNavigateUp()
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
