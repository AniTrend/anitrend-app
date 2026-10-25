package com.mxt.anitrend.navigation.extension

import android.content.Intent
import androidx.core.content.IntentCompat
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.ScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam

/**
 * Stable wire keys for the exported preview activity parameter family.
 *
 * Each key is a wire contract: it is destination-owned, unique per destination family,
 * and must not change when a parameter class is renamed or moved. A derived key would
 * silently break saved-state restoration, deep-link handling, and persisted intent
 * extras, so the keys are declared explicitly and are reviewable at the call site.
 */
const val ARG_IMAGE_PREVIEW_SCREEN = "arg.image.preview.screen"
const val ARG_GIPHY_PREVIEW_SCREEN = "arg.giphy.preview.screen"
const val ARG_VIDEO_PLAYER_SCREEN = "arg.video.player.screen"

/**
 * Resolves the stable bundle key used for [T].
 *
 * Representative parameter types resolve to their explicit stable string key.
 */
inline fun <reified T : ScreenParam> screenParamKey(): String = when (T::class) {
    ImagePreviewScreenParam::class -> ARG_IMAGE_PREVIEW_SCREEN
    GiphyPreviewScreenParam::class -> ARG_GIPHY_PREVIEW_SCREEN
    VideoPlayerScreenParam::class -> ARG_VIDEO_PLAYER_SCREEN
    else -> T::class.java.name
}

/** Writes [param] into [this] [Intent] under its stable key. */
inline fun <reified T : ScreenParam> Intent.putScreenParam(param: T): Intent = apply { putExtra(screenParamKey<T>(), param) }

/** Reads a [ScreenParam] of type [T] from [this] [Intent] extra, or null when absent. */
inline fun <reified T : ScreenParam> Intent.screenParam(): T? = IntentCompat.getParcelableExtra(this, screenParamKey<T>(), T::class.java)
