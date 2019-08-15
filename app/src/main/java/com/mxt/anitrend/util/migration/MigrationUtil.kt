package com.mxt.anitrend.util.migration

import android.util.Log
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.util.ApplicationPref
import com.mxt.anitrend.util.migration.contract.IMigrationUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

class MigrationUtil private constructor(
        private val migrations: List<Migration>
) : IMigrationUtil, KoinComponent {

    private val applicationPref by inject<ApplicationPref>()

    /**
     * Applies migration of the application if necessary
     */
    override fun applyMigration(): Boolean {
        if (applicationPref.isUpdated) {
            Log.d(TAG, "Application has been updated: from ${applicationPref.versionCode} - ${BuildConfig.VERSION_CODE}, checking for migration scripts")
            val applicableMigrations = migrations.takeWhile {
                applicationPref.versionCode >= it.startVersion && it.endVersion <= BuildConfig.VERSION_CODE
            }
            if (applicableMigrations.isNotEmpty())
                return try {
                    applicableMigrations.forEach {
                        Log.d(TAG, "Applying migration for: ${it.startVersion} - ${it.endVersion}")
                        it.applyMigration(applicationPref)
                    }
                    true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    false
                }
        }
        Log.d(TAG, "No migrations to run for this version of the application")
        return true
    }

    class Builder {
        private val migrations: MutableList<Migration> = ArrayList()

        fun addMigration(migration: Migration): Builder {
            migrations.add(migration)
            return this
        }

        fun build(): MigrationUtil {
            return MigrationUtil(migrations)
        }
    }

    companion object {
        private val TAG = MigrationUtil::class.java.simpleName
    }
}