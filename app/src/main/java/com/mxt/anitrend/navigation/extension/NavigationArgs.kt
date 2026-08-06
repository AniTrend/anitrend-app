package com.mxt.anitrend.navigation.extension

import com.mxt.anitrend.graphql.generated.ActivityType
import com.mxt.anitrend.graphql.generated.MediaType

/**
 * Production parsing helpers for fragment navigation bundles.
 *
 * Fragment destinations that carry no stable identity (feed lists, browse filters)
 * keep their values on the legacy bundle channel, but every read must go through
 * these helpers so the exact `containsKey` / default semantics are captured in one
 * reviewable place and are JVM-testable without a real [android.os.Bundle].
 */
object NavigationArgs {

    /**
     * Mirrors `Bundle.getBoolean(key, default)`: the stored value when the key is
     * present, otherwise [default].
     */
    fun booleanWithDefault(containsKey: Boolean, value: Boolean, default: Boolean): Boolean =
        if (containsKey) value else default

    /**
     * Mirrors `Bundle.getInt(key, default)`: the stored value when the key is
     * present, otherwise [default].
     */
    fun intWithDefault(containsKey: Boolean, value: Int, default: Int): Int =
        if (containsKey) value else default

    /**
     * Mirrors the tri-state `if (bundle.containsKey(key)) bundle.getBoolean(key) else null`
     * pattern: absent keys resolve to null, present keys resolve to the stored value.
     */
    fun optionalBoolean(containsKey: Boolean, value: Boolean): Boolean? =
        if (containsKey) value else null

    /**
     * Mirrors the tri-state `if (bundle.containsKey(key)) bundle.getString(key) else null`
     * pattern: absent keys resolve to null, present keys resolve to the stored value.
     */
    fun optionalString(containsKey: Boolean, value: String?): String? =
        if (containsKey) value else null

    /**
     * Mirrors the tri-state `if (bundle.containsKey(key)) bundle.getInt(key) else null`
     * pattern: absent keys resolve to null, present keys resolve to the stored value.
     */
    fun optionalInt(containsKey: Boolean, value: Int): Int? =
        if (containsKey) value else null

    /**
     * Mirrors the tri-state string-array-list pattern used by browse filters: absent
     * keys resolve to null (semantically absent GraphQL input), present keys resolve
     * to the stored list. Empty lists are normalized to null downstream by the
     * ViewModel so an explicit empty list is never sent as real filter input.
     */
    fun optionalStringList(containsKey: Boolean, value: List<String>?): List<String>? =
        if (containsKey) value else null

    /**
     * Mirrors `bundle.getString(key)?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() }`.
     */
    fun resolveActivityType(raw: String?): ActivityType? =
        raw?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() }

    /**
     * Mirrors `bundle.getString(key)?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }`.
     */
    fun resolveMediaType(raw: String?): MediaType? =
        raw?.let { runCatching { MediaType.valueOf(it) }.getOrNull() }
}
