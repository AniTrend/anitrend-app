package com.mxt.anitrend.util.migration

import com.mxt.anitrend.util.ApplicationPref

/**
 * Creates a new migration between [startVersion] and [endVersion].
 *
 * @param startVersion The start version of the application.
 * @param endVersion The end version of the application after this migration is applied.
 */
abstract class Migration(
        val startVersion: Int,
        val endVersion: Int
) {
    abstract fun applyMigration(applicationPref: ApplicationPref)
}