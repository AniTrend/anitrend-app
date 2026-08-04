package com.mxt.anitrend.view.fragment.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import com.mxt.anitrend.R
import com.mxt.anitrend.extension.applyConfiguredTheme
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.DialogUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.NotifyUtil
import com.mxt.anitrend.util.Settings
import timber.log.Timber

/**
 * Applies the side effects triggered by shared preference changes that
 * originate from the settings screens.
 *
 * Extracted verbatim from the legacy single-screen settings so every
 * preference key keeps its existing behavior.
 */
class SettingsPreferenceChangeHandler(
    private val settings: Settings,
    private val scheduler: JobSchedulerUtil,
    private val presenter: BasePresenter,
) {

    fun handle(
        fragmentActivity: FragmentActivity,
        key: String?,
    ) {
        when (key) {
            fragmentActivity.getString(R.string.pref_key_display_adult_content),
            fragmentActivity.getString(R.string.pref_key_crash_reports),
            fragmentActivity.getString(R.string.pref_key_usage_analytics),
            fragmentActivity.getString(R.string.pref_key_list_view_style),
            -> {
                requireRestartNotice(fragmentActivity)
            }
            fragmentActivity.getString(R.string.pref_key_selected_language) -> {
                val locales = LocaleListCompat.forLanguageTags(settings.userLanguage)
                AppCompatDelegate.setApplicationLocales(locales)
            }
            fragmentActivity.getString(R.string.pref_key_startup_page) -> {
                if (!settings.isAuthenticated) {
                    NotifyUtil.makeText(fragmentActivity, R.string.info_login_req, Toast.LENGTH_SHORT).show()
                } else {
                    requireRestartNotice(fragmentActivity)
                }
            }
            fragmentActivity.getString(R.string.pref_key_app_theme) -> {
                fragmentActivity.applyConfiguredTheme()
            }
            fragmentActivity.getString(R.string.pref_key_sync_frequency) -> {
                scheduler.cancelNotificationJob(fragmentActivity.applicationContext)
                scheduler.cancelTagJob(fragmentActivity.applicationContext)
                scheduler.cancelGenreJob(fragmentActivity.applicationContext)
                scheduler.scheduleNotificationJob(fragmentActivity.applicationContext)
                scheduler.scheduleGenreJob(fragmentActivity.applicationContext)
                scheduler.scheduleTagJob(fragmentActivity.applicationContext)
            }
            fragmentActivity.getString(R.string.pref_key_new_message_notifications) -> {
                if (settings.isNotificationEnabled) {
                    scheduler.scheduleNotificationJob(fragmentActivity.applicationContext)
                } else {
                    scheduler.cancelNotificationJob(fragmentActivity.applicationContext)
                }
            }
            fragmentActivity.getString(R.string.pref_key_update_channel) -> {
                presenter.database.remoteVersion = null
            }
            else -> Timber.i("$key not registered in this sharedPreferenceChange listener")
        }
    }

    private fun requireRestartNotice(fragmentActivity: FragmentActivity) {
        DialogUtil
            .createDefaultDialog(fragmentActivity)
            .setPositiveButton(R.string.Ok) { d, _ -> d.dismiss() }
            .setMessage(R.string.text_application_restart_required)
            .show()
    }
}
