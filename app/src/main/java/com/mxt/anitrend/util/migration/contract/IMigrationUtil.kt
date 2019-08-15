package com.mxt.anitrend.util.migration.contract

interface IMigrationUtil {
    /**
     * Applies migration of the application if necessary
     */
    fun applyMigration(): Boolean
}