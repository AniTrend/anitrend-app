package com.mxt.anitrend.view.activity.base

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.R
import com.mxt.anitrend.databinding.SettingsActivityBinding
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.extension.applyConfiguredTheme
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import org.koin.android.ext.android.inject
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

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

        val binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
    }

    class SettingsFragment :
        PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        private val settings by inject<Settings>()
        private val scheduler by inject<JobSchedulerUtil>()
        private val presenter by inject<BasePresenter>()

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            val resId =
                if (settings.experimentalSettingsScreen) {
                    R.xml.root_preferences_experimental
                } else {
                    R.xml.root_preferences
                }
            setPreferencesFromResource(resId, rootKey)
            findPreference<PreferenceCategory>(getString(R.string.pref_key_privacy))?.isVisible =
                FirebaseApp.getApps(requireContext()).isNotEmpty()
        }

        override fun onResume() {
            super.onResume()
            settings.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            settings.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        private fun requireRestartNotice(fragmentActivity: FragmentActivity) {
            DialogUtil
                .createDefaultDialog(fragmentActivity)
                .setPositiveButton(R.string.Ok) { d, _ -> d.dismiss() }
                .setMessage(R.string.text_application_restart_required)
                .show()
        }

        override fun onSharedPreferenceChanged(
            preferences: SharedPreferences?,
            key: String?,
        ) {
            activity?.apply {
                when (key) {
                    getString(R.string.pref_key_experimental_markdown),
                    getString(R.string.pref_key_experimental_about_screen),
                    getString(R.string.pref_key_experimental_settings_screen),
                    getString(R.string.pref_key_experimental_initial_screens),
                    getString(R.string.pref_key_experimental_manage_library),
                    getString(R.string.pref_key_display_adult_content),
                    getString(R.string.pref_key_crash_reports),
                    getString(R.string.pref_key_usage_analytics),
                    getString(R.string.pref_key_list_view_style),
                    -> {
                        requireRestartNotice(this)
                    }
                    getString(R.string.pref_key_selected_language) -> {
                        val locales = LocaleListCompat.forLanguageTags(settings.userLanguage)
                        AppCompatDelegate.setApplicationLocales(locales)
                    }
                    getString(R.string.pref_key_startup_page) -> {
                        if (!settings.isAuthenticated) {
                            NotifyUtil.makeText(this, R.string.info_login_req, Toast.LENGTH_SHORT).show()
                        } else {
                            requireRestartNotice(this)
                        }
                    }
                    getString(R.string.pref_key_app_theme) -> {
                        applyConfiguredTheme()
                    }
                    getString(R.string.pref_key_sync_frequency) -> {
                        scheduler.cancelNotificationJob(applicationContext)
                        scheduler.cancelTagJob(applicationContext)
                        scheduler.cancelGenreJob(applicationContext)
                        scheduler.scheduleNotificationJob(applicationContext)
                        scheduler.scheduleGenreJob(applicationContext)
                        scheduler.scheduleTagJob(applicationContext)
                    }
                    getString(R.string.pref_key_new_message_notifications) -> {
                        if (settings.isNotificationEnabled) {
                            scheduler.scheduleNotificationJob(applicationContext)
                        } else {
                            scheduler.cancelNotificationJob(applicationContext)
                        }
                    }
                    getString(R.string.pref_key_update_channel) -> {
                        presenter.database.remoteVersion = null
                    }
                    else -> Timber.i("$key not registered in this sharedPreferenceChange listener")
                }
            }
        }
    }
}
