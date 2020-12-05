package com.mxt.anitrend.util.migration

import androidx.annotation.VisibleForTesting
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.util.migration.contract.IMigrationUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class MigrationUtil private constructor(
        private val migrations: List<Migration>
) : IMigrationUtil, KoinComponent {

    private val settings by inject<Settings>()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getMigrationStrategies(): List<Migration> {
        val currentVersion = settings.versionCode
        val minMigrations = migrations.filter { migration ->
            IntRange(
                    migration.startVersion,
                    migration.endVersion
            ).contains(currentVersion)
        }
        val maxMigrations = migrations.filter { migration ->
            IntRange(
                    migration.startVersion,
                    migration.endVersion
            ).contains(BuildConfig.VERSION_CODE)
        }

        return minMigrations + maxMigrations
    }

    /**
     * Applies migration of the application if necessary
     */
    override fun applyMigration(): Boolean {
        if (settings.isUpdated) {
            Timber.tag(TAG).d("Application has been updated: from ${settings.versionCode} - ${BuildConfig.VERSION_CODE}, checking for migration scripts")
            val strategies= getMigrationStrategies()
            if (strategies.isNotEmpty())
                return try {
                    strategies.forEach { strategy ->
                        strategy.applyMigration(settings)
                    }
                    true
                } catch (ex: Exception) {
                    Timber.tag(TAG).e(ex)
                    ex.printStackTrace()
                    false
                }
        }
        Timber.tag(TAG).d("No migrations to run for this version of the application")
        return true
    }

    class Builder {
        private val migrations: MutableList<Migration> = ArrayList()

        fun addMigration(migration: Migration): Builder {
            if (!migrations.contains(migration))
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