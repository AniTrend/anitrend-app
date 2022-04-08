package com.mxt.anitrend.util.migration

import android.content.Context
import com.mxt.anitrend.util.Settings
import timber.log.Timber

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
    open fun applyMigration(context: Context, settings: Settings) {
        Timber.i("Applying migrations for $this")
    }

    /**
     * Indicates whether some other object is "equal to" this one. Implementations must fulfil the following
     * requirements:
     *
     * * Reflexive: for any non-null value `x`, `x.equals(x)` should return true.
     * * Symmetric: for any non-null values `x` and `y`, `x.equals(y)` should return true if and only if `y.equals(x)` returns true.
     * * Transitive:  for any non-null values `x`, `y`, and `z`, if `x.equals(y)` returns true and `y.equals(z)` returns true, then `x.equals(z)` should return true.
     * * Consistent:  for any non-null values `x` and `y`, multiple invocations of `x.equals(y)` consistently return true or consistently return false, provided no information used in `equals` comparisons on the objects is modified.
     * * Never equal to null: for any non-null value `x`, `x.equals(null)` should return false.
     *
     * Read more about [equality](https://kotlinlang.org/docs/reference/equality.html) in Kotlin.
     */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Migration -> startVersion == other.startVersion && endVersion == other.endVersion
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = startVersion
        result = 31 * result + endVersion
        return result
    }

    /**
     * Returns a string representation of the object.
     */
    override fun toString() = "$startVersion - $endVersion"
}