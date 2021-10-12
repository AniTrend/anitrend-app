package com.mxt.anitrend.util.migration

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.mxt.anitrend.analytics.AnalyticsLogging
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.ShortcutUtil
import timber.log.Timber

object Migrations {

    val MIGRATION_101_108 = object : Migration(101, 108) {
        override fun applyMigration(context: Context, settings: Settings) {
            Timber.i("Applying migrations for $this")
            settings.edit {
                clear()
                apply()
            }
            DatabaseHelper().invalidateBoxStores()
            koinOf<JobSchedulerUtil>().cancelNotificationJob(context)
        }
    }

    val MIGRATION_109_134 = object : Migration(109, 134) {
        override fun applyMigration(context: Context, settings: Settings) {
            Timber.i("Applying migrations for $this")
            settings.edit {
                clear()
                apply()
            }
            DatabaseHelper().invalidateBoxStores()
            WebFactory.invalidate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                ShortcutUtil.removeAllDynamicShortcuts(context)

            koinOf<AnalyticsLogging>().resetAnalyticsData()
        }
    }

    val MIGRATION_135_136 = object : Migration(135, 136) {
        override fun applyMigration(context: Context, settings: Settings) {
            Timber.i("Applying migrations for $this")
            settings.isCrashReportsEnabled = true
        }
    }

    val MIGRATION_18400_18500 = object : Migration(18400, 18500) {
        override fun applyMigration(context: Context, settings: Settings) {
            Timber.i("Applying migrations for $this")
            settings.edit {
                // A small error in the italian translation which was using the
                // string below as a key for notification preference
                remove("Notifiche di nuovo messaggio")
            }
        }
    }
}