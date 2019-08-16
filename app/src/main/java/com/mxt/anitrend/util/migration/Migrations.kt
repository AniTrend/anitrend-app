package com.mxt.anitrend.util.migration

import android.os.Build
import androidx.core.content.edit
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.appContext
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.ShortcutUtil
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

object Migrations : KoinComponent {

    private val supportAnalytics by inject<ISupportAnalytics>()

    val MIGRATION_90_94 = object : Migration(90, 94) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying test migration from 90 - 94")
        }
    }

    val MIGRATION_95_100 = object : Migration(95, 100) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying test migration from 95 - 100")
        }
    }

    val MIGRATION_101_108 = object : Migration(101, 108) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying test migration from 101 - 109")
        }
    }

    val MIGRATION_109_132 = object : Migration(109, 132) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying migration from 109 - 132")
            settings.sharedPreferences.edit {
                clear()
                apply()
            }
            DatabaseHelper().invalidateBoxStores()
            JobSchedulerUtil.cancelJob()
            WebFactory.invalidate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                ShortcutUtil.removeAllDynamicShortcuts(appContext)
            supportAnalytics.resetAnalyticsData()
        }
    }

    val MIGRATION_133_135 = object : Migration(133, 135) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying migration from 133 - 135")
        }
    }

    val MIGRATION_136_140 = object : Migration(136, 140) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying migration from 136 - 140")
        }
    }

    val MIGRATION_141_145 = object : Migration(141, 145) {
        override fun applyMigration(settings: Settings) {
            Timber.i("Applying migration from 141 - 145")
        }
    }
}