package com.mxt.anitrend.navigation.extension

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import android.os.Bundle
import com.mxt.anitrend.R
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mxt.anitrend.navigation.model.CommentScreenParam
import com.mxt.anitrend.navigation.model.CharacterScreenParam
import com.mxt.anitrend.navigation.model.MediaScreenParam
import com.mxt.anitrend.navigation.model.StudioScreenParam
import com.mxt.anitrend.navigation.model.StaffScreenParam
import com.mxt.anitrend.navigation.model.UserScreenParam
import com.mxt.anitrend.view.fragment.list.MediaListFragment
import com.mxt.anitrend.view.fragment.list.MediaListOrigin
import com.mxt.anitrend.view.fragment.list.WatchListFragment

/**
 * Canonical UI-layer routes for destinations already migrated to the root host.
 *
 * These extensions deliberately remain on [NavController]'s UI boundary. They
 * are not available to ViewModels, repositories, stores, adapters, or custom
 * views.
 */
fun NavController.navigateToAbout() {
    navigate(R.id.action_global_aboutFragment, null, destinationOptions())
}

fun NavController.navigateToChangelog() {
    navigate(R.id.action_global_changelogFragment, null, destinationOptions())
}

fun NavController.navigateToLogging() {
    navigate(R.id.action_global_loggingFragment, null, destinationOptions())
}

fun NavController.navigateToSettings() {
    navigate(R.id.action_global_settingsHubFragment, null, destinationOptions())
}

fun NavController.navigateToNotifications() {
    navigate(R.id.action_global_notificationFragment, null, destinationOptions())
}

fun NavController.navigateToMessages() {
    navigate(R.id.action_global_messageFragment, null, destinationOptions())
}

fun NavController.navigateToSharedContent(arguments: Bundle = Bundle.EMPTY) {
    navigate(R.id.action_global_sharedContentFragment, arguments, destinationOptions())
}

fun NavController.navigateToComment(param: CommentScreenParam) {
    navigate(R.id.action_global_commentFragment, param.asBundle(), destinationOptions())
}

fun NavController.navigateToStudio(param: StudioScreenParam) {
    navigate(R.id.action_global_studioFragment, param.asBundle(), destinationOptions())
}

fun NavController.navigateToCharacter(param: CharacterScreenParam) {
    navigate(R.id.action_global_characterFragment, param.asBundle(), destinationOptions())
}

fun NavController.navigateToStaff(param: StaffScreenParam) {
    navigate(R.id.action_global_staffFragment, param.asBundle(), destinationOptions())
}

fun NavController.navigateToProfile(param: UserScreenParam) {
    navigate(
        R.id.action_global_profileFragment,
        param.asBundle().apply {
            putLong(com.mxt.anitrend.util.KeyUtil.arg_id, param.userId)
            param.initialName?.let { putString(com.mxt.anitrend.util.KeyUtil.arg_userName, it) }
        },
        destinationOptions(),
    )
}

fun NavController.navigateToMedia(param: MediaScreenParam) {
    navigate(
        R.id.action_global_mediaFragment,
        param.asBundle().apply {
            putLong(com.mxt.anitrend.util.KeyUtil.arg_id, param.mediaId)
            param.mediaType?.let { putString(com.mxt.anitrend.util.KeyUtil.arg_mediaType, it) }
        },
        destinationOptions(),
    )
}

/**
 * Pushes the media list with an explicit route origin (NFR-002). The origin is
 * carried through the destination arguments and read by the host's
 * top-level/back policy; the legacy [MediaListFragment.ARG_UNIFIED_DESTINATION]
 * flag stays written for its menu-visibility consumer and is not the origin
 * contract.
 *
 * NFR-007 invariant: the [MediaListOrigin.ROOT] path must apply the freshly
 * selected [mediaType] on every drawer My Anime/My Manga navigation. Navigation
 * 2.9 `restoreState(true)` would otherwise resurrect the saved back stack entry
 * from the previous drawer media list, whose saved arguments carry the stale
 * media type (repro: My Anime -> Feed -> My Manga restores ANIME). The ROOT
 * path therefore uses [mediaListRootDestinationOptions], which keeps the root
 * popUpTo/drawer-state behavior but disables state restoration for this
 * destination, so the new arguments always win. All other root destinations
 * keep normal state restoration via [rootDestinationOptions].
 *
 * @param origin [MediaListOrigin.ROOT] only for drawer My Anime/My Manga;
 * every other producer pushes with the default [MediaListOrigin.PUSHED].
 */
fun NavController.navigateToMediaList(
    param: UserScreenParam,
    mediaType: String? = null,
    origin: MediaListOrigin = MediaListOrigin.PUSHED,
) {
    navigate(
        R.id.action_global_mediaListFragment,
        param.asBundle().apply {
            putLong(com.mxt.anitrend.util.KeyUtil.arg_id, param.userId)
            param.initialName?.let { putString(com.mxt.anitrend.util.KeyUtil.arg_userName, it) }
            mediaType?.let { putString(com.mxt.anitrend.util.KeyUtil.arg_mediaType, it) }
            putString(MediaListFragment.ARG_MEDIA_LIST_ORIGIN, origin.name)
            putBoolean(MediaListFragment.ARG_UNIFIED_DESTINATION, true)
        },
        if (origin == MediaListOrigin.ROOT) mediaListRootDestinationOptions() else destinationOptions(),
    )
}

/**
 * Drawer My Anime/My Manga route (NFR-007). Uses the root media-list options so
 * the selected [mediaType] is always applied instead of a restored stale entry;
 * see [navigateToMediaList].
 */
fun NavController.navigateToRootMediaList(
    param: UserScreenParam,
    mediaType: String? = null,
) {
    navigateToMediaList(param, mediaType, origin = MediaListOrigin.ROOT)
}

fun NavController.navigateToFavourites(param: UserScreenParam) {
    navigate(
        R.id.action_global_favouriteFragment,
        param.asBundle().apply {
            putLong(com.mxt.anitrend.util.KeyUtil.arg_id, param.userId)
            param.initialName?.let { putString(com.mxt.anitrend.util.KeyUtil.arg_userName, it) }
        },
        destinationOptions(),
    )
}

fun NavController.navigateToSearch(query: String?) {
    navigate(
        R.id.action_global_searchFragment,
        Bundle().apply { query?.let { putString(com.mxt.anitrend.util.KeyUtil.arg_search, it) } },
        destinationOptions(),
    )
}

fun NavController.navigateToFeed() {
    navigate(R.id.action_global_feedFragment, null, rootDestinationOptions())
}

fun NavController.navigateToAnime() {
    navigate(R.id.action_global_animeFragment, null, rootDestinationOptions())
}

fun NavController.navigateToManga() {
    navigate(R.id.action_global_mangaFragment, null, rootDestinationOptions())
}

fun NavController.navigateToAiring() {
    navigate(R.id.action_global_airingFragment, null, rootDestinationOptions())
}

fun NavController.navigateToHub() {
    navigate(R.id.action_global_hubFragment, null, rootDestinationOptions())
}

fun NavController.navigateToWatchList(popular: Boolean) {
    navigate(
        R.id.action_global_watchListFragment,
        Bundle().apply {
            putBoolean(WatchListFragment.ARG_FEED_ROUTE, true)
            putBoolean(com.mxt.anitrend.util.KeyUtil.arg_popular, popular)
        },
        NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .build(),
    )
}

fun NavController.navigateToReviews() {
    navigate(R.id.action_global_reviewFragment, null, rootDestinationOptions())
}

fun NavController.navigateToTrending() {
    navigate(R.id.action_global_trendingFragment, null, rootDestinationOptions())
}

fun Fragment.navigateToCharacter(param: CharacterScreenParam) {
    findNavController().navigateToCharacter(param)
}

fun Fragment.navigateToComment(param: CommentScreenParam) {
    findNavController().navigateToComment(param)
}

fun Fragment.navigateToStudio(param: StudioScreenParam) {
    findNavController().navigateToStudio(param)
}

fun Fragment.navigateToStaff(param: StaffScreenParam) {
    findNavController().navigateToStaff(param)
}

fun Fragment.navigateToProfile(param: UserScreenParam) {
    findNavController().navigateToProfile(param)
}

fun Fragment.navigateToMedia(param: MediaScreenParam) {
    findNavController().navigateToMedia(param)
}

fun Fragment.navigateToFavourites(param: UserScreenParam) {
    findNavController().navigateToFavourites(param)
}

fun Fragment.navigateToSearch(query: String?) {
    findNavController().navigateToSearch(query)
}

fun NavController.navigateToMediaBrowse(arguments: Bundle) {
    navigate(R.id.action_global_mediaBrowseFragment, arguments, destinationOptions())
}

private fun destinationOptions(): NavOptions = NavOptions.Builder()
    .setLaunchSingleTop(true)
    .setRestoreState(true)
    .build()

private fun rootDestinationOptions(): NavOptions = NavOptions.Builder()
    .setLaunchSingleTop(true)
    .setRestoreState(true)
    .setPopUpTo(R.id.animeFragment, false, true)
    .build()

/**
 * NFR-007: root (drawer) media-list navigation must not restore the saved back
 * stack entry, because that entry carries the previous drawer selection's
 * arguments. With `restoreState(false)` every My Anime/My Manga navigation
 * creates a fresh entry with the newly selected media type, while keeping the
 * root popUpTo (back to the graph root) and `saveState` (drawer state of other
 * root destinations is still preserved).
 */
private fun mediaListRootDestinationOptions(): NavOptions = NavOptions.Builder()
    .setLaunchSingleTop(true)
    .setRestoreState(false)
    .setPopUpTo(R.id.animeFragment, false, true)
    .build()
