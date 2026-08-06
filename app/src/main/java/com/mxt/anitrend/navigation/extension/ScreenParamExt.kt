package com.mxt.anitrend.navigation.extension

import android.content.Intent
import android.os.Bundle
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.GiphyPreviewScreenParam
import com.mxt.anitrend.navigation.model.ImagePreviewScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.ReviewScreenParam
import com.mxt.anitrend.navigation.model.ScreenParam
import com.mxt.anitrend.navigation.model.SettingsCategoryScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.TrailerScreenParam
import com.mxt.anitrend.navigation.model.UserListScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.navigation.model.VideoPlayerScreenParam

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
const val ARG_REVIEW_SCREEN = "arg.review.screen"
const val ARG_CHARACTER_SCREEN = "arg.character.screen"
const val ARG_STAFF_SCREEN = "arg.staff.screen"
const val ARG_IMAGE_PREVIEW_SCREEN = "arg.image.preview.screen"
const val ARG_GIPHY_PREVIEW_SCREEN = "arg.giphy.preview.screen"
const val ARG_VIDEO_PLAYER_SCREEN = "arg.video.player.screen"
const val ARG_USER_LIST_SCREEN = "arg.user.list.screen"
const val ARG_TRAILER_SCREEN = "arg.trailer.screen"
const val ARG_SETTINGS_CATEGORY_SCREEN = "arg.settings.category.screen"

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
    ReviewScreenParam::class -> ARG_REVIEW_SCREEN
    CharacterScreenParam::class -> ARG_CHARACTER_SCREEN
    StaffScreenParam::class -> ARG_STAFF_SCREEN
    ImagePreviewScreenParam::class -> ARG_IMAGE_PREVIEW_SCREEN
    GiphyPreviewScreenParam::class -> ARG_GIPHY_PREVIEW_SCREEN
    VideoPlayerScreenParam::class -> ARG_VIDEO_PLAYER_SCREEN
    UserListScreenParam::class -> ARG_USER_LIST_SCREEN
    TrailerScreenParam::class -> ARG_TRAILER_SCREEN
    SettingsCategoryScreenParam::class -> ARG_SETTINGS_CATEGORY_SCREEN
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
