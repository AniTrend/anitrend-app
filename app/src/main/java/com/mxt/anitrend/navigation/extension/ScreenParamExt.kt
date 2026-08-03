package com.mxt.anitrend.navigation.extension

import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import com.mxt.anitrend.navigation.model.ScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam

/**
 * Stable wire keys for the representative screen parameter family.
 *
 * Each key is a wire contract: it is destination-owned, unique per destination family,
 * and must not change when a parameter class is renamed or moved. A derived key would
 * silently break saved-state restoration, deep-link handling, and persisted intent
 * extras, so the keys are declared explicitly and are reviewable at the call site.
 */
const val ARG_USER_SCREEN = "arg.user.screen"
const val ARG_MEDIA_SCREEN = "arg.media.screen"
const val ARG_COMMENT_SCREEN = "arg.comment.screen"
const val ARG_STUDIO_SCREEN = "arg.studio.screen"
const val ARG_FEED_COMPOSER_SCREEN = "arg.feed.composer.screen"
const val ARG_REVIEW_SCREEN = "arg.review.screen"

/**
 * Resolves the stable bundle key used for [T].
 *
 * Representative parameter types resolve to their explicit stable string key. Any other
 * [ScreenParam] falls back to its fully qualified class name; that secondary option is
 * only intended for generic, non-destination-specific call sites.
 */
inline fun <reified T : ScreenParam> screenParamKey(): String = when (T::class) {
    UserScreenParam::class -> ARG_USER_SCREEN
    MediaScreenParam::class -> ARG_MEDIA_SCREEN
    CommentScreenParam::class -> ARG_COMMENT_SCREEN
    StudioScreenParam::class -> ARG_STUDIO_SCREEN
    FeedComposerScreenParam::class -> ARG_FEED_COMPOSER_SCREEN
    ReviewScreenParam::class -> ARG_REVIEW_SCREEN
    else -> T::class.java.name
}

/** Writes [this] [ScreenParam] into a new [Bundle] under its stable key. */
inline fun <reified T : ScreenParam> T.asBundle(): Bundle = Bundle().apply { putParcelable(screenParamKey<T>(), this@asBundle) }

/** Reads a [ScreenParam] of type [T] from [this] [Bundle], or null when absent. */
inline fun <reified T : ScreenParam> Bundle.screenParam(): T? = BundleCompat.getParcelable(this, screenParamKey<T>(), T::class.java)

/** Writes [param] into [this] [Intent] under its stable key. */
inline fun <reified T : ScreenParam> Intent.putScreenParam(param: T): Intent = apply { putExtra(screenParamKey<T>(), param) }

/** Reads a [ScreenParam] of type [T] from [this] [Intent] extra, or null when absent. */
inline fun <reified T : ScreenParam> Intent.screenParam(): T? = IntentCompat.getParcelableExtra(this, screenParamKey<T>(), T::class.java)
