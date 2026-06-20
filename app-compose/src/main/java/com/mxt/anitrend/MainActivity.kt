package com.mxt.anitrend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mxt.anitrend.data.auth.AuthRepository
import com.mxt.anitrend.data.onboarding.OnboardingPreferences
import com.mxt.anitrend.navigation.About
import com.mxt.anitrend.navigation.ActivityDetail
import com.mxt.anitrend.navigation.Airing
import com.mxt.anitrend.navigation.AppRoute
import com.mxt.anitrend.navigation.Browse
import com.mxt.anitrend.navigation.CharacterDetail
import com.mxt.anitrend.navigation.Composer
import com.mxt.anitrend.navigation.Detail
import com.mxt.anitrend.navigation.Favourites
import com.mxt.anitrend.navigation.Feed
import com.mxt.anitrend.navigation.Genres
import com.mxt.anitrend.navigation.Giphy
import com.mxt.anitrend.navigation.ImagePreview
import com.mxt.anitrend.navigation.LogViewer
import com.mxt.anitrend.navigation.Login
import com.mxt.anitrend.navigation.MediaListEdit
import com.mxt.anitrend.navigation.Notifications
import com.mxt.anitrend.navigation.Profile
import com.mxt.anitrend.navigation.ReviewReader
import com.mxt.anitrend.navigation.Reviews
import com.mxt.anitrend.navigation.Search
import com.mxt.anitrend.navigation.Settings
import com.mxt.anitrend.navigation.SharedContent
import com.mxt.anitrend.navigation.Splash
import com.mxt.anitrend.navigation.SpoilerEditor
import com.mxt.anitrend.navigation.StaffDetail
import com.mxt.anitrend.navigation.StudioDetail
import com.mxt.anitrend.navigation.Threads
import com.mxt.anitrend.navigation.UserFavourites
import com.mxt.anitrend.navigation.WatchList
import com.mxt.anitrend.navigation.Welcome
import com.mxt.anitrend.navigation.YouTube
import com.mxt.anitrend.theme.AniTrendTheme
import com.mxt.anitrend.ui.about.AboutScreen
import com.mxt.anitrend.ui.activitydetail.ActivityDetailScreen
import com.mxt.anitrend.ui.browse.BrowseScreen
import com.mxt.anitrend.ui.character.CharacterDetailScreen
import com.mxt.anitrend.ui.composer.ComposerSheet
import com.mxt.anitrend.ui.detail.DetailScreen
import com.mxt.anitrend.ui.favourite.FavouritesScreen
import com.mxt.anitrend.ui.favourite.UserFavouritesScreen
import com.mxt.anitrend.ui.feed.FeedScreen
import com.mxt.anitrend.ui.forum.ThreadsScreen
import com.mxt.anitrend.ui.genre.GenreListScreen
import com.mxt.anitrend.ui.giphy.GiphySheet
import com.mxt.anitrend.ui.image.ImagePreviewScreen
import com.mxt.anitrend.ui.log.LogViewerScreen
import com.mxt.anitrend.ui.login.LoginScreen
import com.mxt.anitrend.ui.medialist.MediaListEditScreen
import com.mxt.anitrend.ui.notification.NotificationsScreen
import com.mxt.anitrend.ui.profile.ProfileScreen
import com.mxt.anitrend.ui.review.ReviewReaderScreen
import com.mxt.anitrend.ui.review.ReviewScreen
import com.mxt.anitrend.ui.schedule.AiringScreen
import com.mxt.anitrend.ui.search.SearchScreen
import com.mxt.anitrend.ui.settings.SettingsScreen
import com.mxt.anitrend.ui.shared.SharedContentScreen
import com.mxt.anitrend.ui.splash.SplashScreen
import com.mxt.anitrend.ui.spoiler.SpoilerScreen
import com.mxt.anitrend.ui.staff.StaffDetailScreen
import com.mxt.anitrend.ui.studio.StudioDetailScreen
import com.mxt.anitrend.ui.watchlist.WatchListScreen
import com.mxt.anitrend.ui.welcome.WelcomeScreen
import com.mxt.anitrend.ui.youtube.YouTubeScreen
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authRepository = remember { GlobalContext.get().get<AuthRepository>() }
            val onboardingPreferences = remember { GlobalContext.get().get<OnboardingPreferences>() }
            val startRoute = if (onboardingPreferences.hasCompletedOnboarding) Splash else Welcome
            val backStack = remember { mutableStateListOf<AppRoute>(startRoute) }
            val scope = rememberCoroutineScope()

            AniTrendTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<Welcome> {
                            WelcomeScreen(
                                onboardingPreferences = onboardingPreferences,
                                onGetStarted = {
                                    backStack.clear()
                                    backStack.add(Splash)
                                },
                            )
                        }
                        entry<Splash> {
                            SplashScreen(
                                authRepository = authRepository,
                                onNavigateToMain = {
                                    backStack.clear()
                                    backStack.add(Feed)
                                },
                                onNavigateToLogin = {
                                    backStack.clear()
                                    backStack.add(Login)
                                },
                            )
                        }
                        entry<Login> {
                            LoginScreen(
                                authRepository = authRepository,
                                onLoginSuccess = {
                                    backStack.clear()
                                    backStack.add(Feed)
                                },
                            )
                        }
                        entry<Feed> {
                            FeedScreen(
                                onNavigateToSearch = { backStack.add(Search) },
                                onNavigateToSettings = { backStack.add(Settings) },
                                onNavigateToNotifications = { backStack.add(Notifications) },
                                onNavigateToProfile = { backStack.add(Profile) },
                                onNavigateToFavourites = { backStack.add(Favourites) },
                                onNavigateToDetail = { activityId ->
                                    backStack.add(ActivityDetail(activityId))
                                },
                                onNavigateToComposer = { backStack.add(Composer) },
                                onNavigateToThreads = { backStack.add(Threads) },
                    onNavigateToBrowse = { backStack.add(Browse) },
                    onNavigateToWatchList = { backStack.add(WatchList) },
                )
                        }
                        entry<Detail> { key ->
                            DetailScreen(
                                mediaId = key.mediaId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToTrailer = { videoId ->
                                    backStack.add(YouTube(videoId))
                                },
                                onNavigateToEditList = { mediaId, mediaTitle ->
                                    backStack.add(MediaListEdit(mediaId, mediaTitle))
                                },
                            )
                        }
                        entry<ActivityDetail> { key ->
                            ActivityDetailScreen(
                                activityId = key.activityId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToMedia = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                            )
                        }
                        entry<CharacterDetail> { key ->
                            CharacterDetailScreen(
                                characterId = key.characterId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToMedia = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                            )
                        }
                        entry<StaffDetail> { key ->
                            StaffDetailScreen(
                                staffId = key.staffId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToMedia = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                            )
                        }
                        entry<StudioDetail> { key ->
                            StudioDetailScreen(
                                studioId = key.studioId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToMedia = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                            )
                        }
                        entry<Search> {
                            SearchScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToDetail = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                                onNavigateToCharacter = { characterId ->
                                    backStack.add(CharacterDetail(characterId))
                                },
                                onNavigateToStaff = { staffId ->
                                    backStack.add(StaffDetail(staffId))
                                },
                                onNavigateToStudio = { studioId ->
                                    backStack.add(StudioDetail(studioId))
                                },
                            )
                        }
                        entry<Profile> {
                            ProfileScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToFavourites = { backStack.add(Favourites) },
                                onNavigateToAiring = { backStack.add(Airing) },
                                onNavigateToReviews = { backStack.add(Reviews) },
                                onNavigateToThreads = { backStack.add(Threads) },
                                onNavigateToGenres = { backStack.add(Genres) },
                                onNavigateToWatchList = { backStack.add(WatchList) },
                                onNavigateToUserFavourites = { backStack.add(UserFavourites) },
                                onNavigateToDetail = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                            )
                        }
                    entry<Favourites> {
                        FavouritesScreen(
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onNavigateToMedia = { mediaId ->
                                backStack.add(Detail(mediaId))
                            },
                        )
                    }
                    entry<WatchList> {
                        WatchListScreen(
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onNavigateToEditList = { mediaId, mediaTitle, listEntryId ->
                                backStack.add(MediaListEdit(mediaId, mediaTitle, listEntryId))
                            },
                        )
                    }
                    entry<UserFavourites> {
                        UserFavouritesScreen(
                            onNavigateBack = { backStack.removeLastOrNull() },
                            onNavigateToMedia = { mediaId ->
                                backStack.add(Detail(mediaId))
                            },
                            onNavigateToCharacter = { characterId ->
                                backStack.add(CharacterDetail(characterId))
                            },
                            onNavigateToStaff = { staffId ->
                                backStack.add(StaffDetail(staffId))
                            },
                            onNavigateToStudio = { studioId ->
                                backStack.add(StudioDetail(studioId))
                            },
                        )
                    }
                        entry<Settings> {
                            SettingsScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToProfile = { backStack.add(Profile) },
                                onNavigateToAbout = { backStack.add(About) },
                                onNavigateToLogs = { backStack.add(LogViewer) },
                                onLogout = {
                                    scope.launch {
                                        authRepository.markLoggedOut()
                                        backStack.clear()
                                        backStack.add(Splash)
                                    }
                                },
                            )
                        }
                        entry<Notifications> {
                            NotificationsScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToMedia = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                                onNavigateToActivity = { activityId ->
                                    backStack.add(ActivityDetail(activityId))
                                },
                            )
                        }
                        entry<About> {
                            AboutScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Composer> {
                            ComposerSheet(
                                onDismiss = { backStack.removeLastOrNull() },
                                onPost = { backStack.removeLastOrNull() },
                                onNavigateToGiphy = { backStack.add(Giphy) },
                            )
                        }
                        entry<Airing> {
                            AiringScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Reviews> {
                            ReviewScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<SpoilerEditor> {
                            SpoilerScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<ReviewReader> { key ->
                            ReviewReaderScreen(
                                reviewText = key.reviewText,
                                rating = key.rating,
                                userName = key.userName,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<SharedContent> {
                            SharedContentScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<LogViewer> {
                            LogViewerScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Browse> {
                            BrowseScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToDetail = { mediaId ->
                                    backStack.add(Detail(mediaId))
                                },
                            )
                        }
                        entry<Threads> {
                            ThreadsScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<MediaListEdit> { key ->
                            MediaListEditScreen(
                                mediaId = key.mediaId,
                                mediaTitle = key.mediaTitle,
                                listEntryId = key.listEntryId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<ImagePreview> { key ->
                            ImagePreviewScreen(
                                imageUrl = key.imageUrl,
                                title = key.title,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Genres> {
                            GenreListScreen(
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Giphy> {
                            GiphySheet(
                                onDismiss = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<YouTube> { key ->
                            YouTubeScreen(
                                videoId = key.videoId,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }
                    },
                )
            }
        }
    }
}
