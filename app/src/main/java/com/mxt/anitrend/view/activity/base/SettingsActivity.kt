package com.mxt.anitrend.view.activity.base

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceFragmentCompat
import butterknife.BindView
import butterknife.ButterKnife
import com.mxt.anitrend.R
import com.mxt.anitrend.base.custom.activity.ActivityBase
import com.mxt.anitrend.extension.applyConfiguredTheme
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import org.koin.android.ext.android.inject
import timber.log.Timber

class SettingsActivity : ActivityBase<Nothing, BasePresenter>() {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        onActivityReady()
    }

    /**
     * Make decisions, check for permissions or fire background threads from this method
     * N.B. Must be called after onPostCreate
     */
    override fun onActivityReady() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateUI()
    }

    override fun updateUI() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

    }

    override fun makeRequest() {

    }

    class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val settings by inject<Settings>()
        private val scheduler by inject<JobSchedulerUtil>()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        /**
         * Called when the fragment is visible to the user and actively running.
         * This is generally
         * tied to [Activity.onResume] of the containing
         * Activity's lifecycle.
         */
        override fun onResume() {
            super.onResume()
            settings.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        /**
         * Called when the Fragment is no longer resumed.  This is generally
         * tied to [Activity.onPause] of the containing
         * Activity's lifecycle.
         */
        override fun onPause() {
            settings.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        private fun requireRestartNotice(fragmentActivity: FragmentActivity) {
            DialogUtil.createDefaultDialog(fragmentActivity)
                .autoDismiss(true)
                .positiveText(R.string.Ok)
                .content(R.string.text_application_restart_required)
                .show()
        }

        override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
            activity?.apply {
                when (key) {
                    getString(R.string.pref_key_display_adult_content),
                    getString(R.string.pref_key_notification_work_around),
                    getString(R.string.pref_key_crash_reports),
                    getString(R.string.pref_key_usage_analytics),
                    getString(R.string.pref_key_selected_language)-> {
                        requireRestartNotice(this)
                    }
                    getString(R.string.pref_key_startup_page) -> {
                        if (!settings.isAuthenticated)
                            NotifyUtil.makeText(this, R.string.info_login_req, Toast.LENGTH_SHORT).show()
                        else
                            requireRestartNotice(this)
                    }
                    getString(R.string.pref_key_app_theme) -> {
                        applyConfiguredTheme()
                    }
                    getString(R.string.pref_key_sync_frequency) -> {
                        scheduler.scheduleJob()
                    }
                    getString(R.string.pref_key_new_message_notifications) -> {
                        if (settings.isNotificationEnabled)
                            scheduler.scheduleJob()
                        else
                            scheduler.cancelJob()
                    }
                    else -> Timber.i("$key not registered in this sharedPreferenceChange listener")
                }
            }
        }
    }
}