package com.mxt.anitrend.navigation

import androidx.navigation3.runtime.NavKey

sealed class AppRoute : NavKey

data object Splash : AppRoute()

data object Welcome : AppRoute()

data object Login : AppRoute()

data object Feed : AppRoute()

data class Detail(val mediaId: Long) : AppRoute()

data class CharacterDetail(val characterId: Long) : AppRoute()
data class StaffDetail(val staffId: Long) : AppRoute()
data class StudioDetail(val studioId: Long) : AppRoute()

data class ActivityDetail(val activityId: Long) : AppRoute()

data object Search : AppRoute()

data object Profile : AppRoute()

data object Notifications : AppRoute()

data object Settings : AppRoute()

data object Favourites : AppRoute()
data object About : AppRoute()

data object Composer : AppRoute()
data object Airing : AppRoute()
data object Reviews : AppRoute()
data object WatchList : AppRoute()
data object UserFavourites : AppRoute()

data class ImagePreview(val imageUrl: String, val title: String) : AppRoute()
data object SpoilerEditor : AppRoute()
data class ReviewReader(val reviewText: String, val rating: Int, val userName: String) : AppRoute()
data object SharedContent : AppRoute()
data object LogViewer : AppRoute()
data object Browse : AppRoute()
data object Threads : AppRoute()
data object Genres : AppRoute()
data object Giphy : AppRoute()
data class YouTube(val videoId: String) : AppRoute()
data class MediaListEdit(val mediaId: Long, val mediaTitle: String, val listEntryId: Long? = null) : AppRoute()
