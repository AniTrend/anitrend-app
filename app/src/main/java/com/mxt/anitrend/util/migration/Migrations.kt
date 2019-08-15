package com.mxt.anitrend.util.migration

import android.os.Build
import androidx.core.content.edit
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.appContext
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.util.AnalyticsUtil
import com.mxt.anitrend.util.ApplicationPref
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.ShortcutUtil
import org.koin.core.KoinComponent

object Migrations : KoinComponent {

    val MIGRATION_109_132 = object : Migration(109, 132) {
        override fun applyMigration(applicationPref: ApplicationPref) {
            applicationPref.sharedPreferences.edit {
                clear()
                apply()
            }
            DatabaseHelper().invalidateBoxStores()
            JobSchedulerUtil.cancelJob()
            WebFactory.invalidate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                ShortcutUtil.removeAllDynamicShortcuts(appContext)
            AnalyticsUtil.clearSession()
        }
    }
}