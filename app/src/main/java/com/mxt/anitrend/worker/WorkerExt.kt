package com.mxt.anitrend.worker

import com.mxt.anitrend.model.entity.container.body.AniListContainer

/**
 * Safely unwraps a GraphQL response body that may or may not be wrapped in
 * [AniListContainer], depending on converter behavior.
 *
 * Three cases handled:
 * - `null` → returns null
 * - [AniListContainer] → unwraps via `body.data?.result` with a safe cast
 * - any other runtime type (e.g. raw list from converter) → safe cast directly
 */
@Suppress("UNCHECKED_CAST")
fun <T> unwrapBody(body: Any?): T? = when (body) {
    null -> null
    is AniListContainer<*> -> body.data?.result as? T
    else -> body as? T
}
