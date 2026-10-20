/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.FirebaseApp
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.navigation.AppNavKey
import com.mxt.anitrend.navigation.AppNavKey.SettingsCategory
import com.mxt.anitrend.ui.about.AboutScreen
import com.mxt.anitrend.ui.changelog.ChangelogScreen
import com.mxt.anitrend.ui.logging.LoggingScreen
import com.mxt.anitrend.ui.markdown.createMarkwon
import com.mxt.anitrend.ui.markdown.rememberMarkdownLinkHandler
import com.mxt.anitrend.ui.sheet.SpoilerSheet
import com.mxt.anitrend.ui.share.SharedContentScreen
import com.mxt.anitrend.util.Settings
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.mxt.anitrend.viewmodel.ChangelogViewModel
import com.mxt.anitrend.viewmodel.LoggingViewModel
import com.mxt.anitrend.viewmodel.NotificationViewModel
import com.mxt.anitrend.viewmodel.EpisodesViewModel
import com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor
import com.mxt.anitrend.domain.feed.interactor.SaveFeedRequest
import kotlinx.coroutines.launch
import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.FileProvider
import com.mxt.anitrend.data.mapper.toMediaList
import com.mxt.anitrend.domain.medialist.model.MediaListRecord
import com.mxt.anitrend.domain.model.ReviewRecord
import com.mxt.anitrend.domain.model.UserSummaryRecord
import com.mxt.anitrend.domain.model.buildIncrementMediaProgressCommand
import com.mxt.anitrend.domain.model.toDraft
import com.mxt.anitrend.domain.model.toSaveMediaListEntryCommand
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.ui.filter.FilterOptionSheet
import com.mxt.anitrend.ui.manage.ManageListSheet
import com.mxt.anitrend.ui.review.ReviewReaderSheet
import com.mxt.anitrend.ui.sheet.UserListSheet
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.viewmodel.MediaListMutationViewModel
import com.mxt.anitrend.viewmodel.MediaListViewModel
import com.mxt.anitrend.viewmodel.UserListViewModel

/**
 * Route dispatcher: renders the Compose screen owned by one route key.
 *
 * ViewModels resolve through [koinViewModel] against the NavEntry's
 * decorated [androidx.lifecycle.ViewModelStoreOwner], so each visit owns
 * its state and clears on pop. Navigation callbacks flow from the shell.
 */
@Composable
internal fun AppEntryContent(
    route: AppNavKey,
    isAuthenticated: Boolean,
    push: (AppNavKey) -> Unit,
    onOpenDrawer: () -> Unit,
    onBack: () -> Unit,
) {
    when (route) {
        is AppNavKey.Settings -> SettingsHost(
            onOpenCategory = { push(SettingsCategory(it)) },
            onOpenChangelog = { push(AppNavKey.Changelog) },
            onOpenLogs = { push(AppNavKey.Logging) },
            onBackClick = onBack,
        )
        is SettingsCategory -> SettingsCategoryHost(
            categoryId = route.categoryId,
            onBackClick = onBack,
        )
        AppNavKey.About -> AboutHost(
            onOpenChangelog = { push(AppNavKey.Changelog) },
            onBackClick = onBack,
        )
        AppNavKey.Changelog -> ChangelogHost(onBackClick = onBack)
        AppNavKey.Logging -> LoggingHost(onBackClick = onBack)
        AppNavKey.Notifications -> NotificationsHost(
            onOpenActivity = { push(AppNavKey.Comment(it)) },
            onOpenMedia = { id -> push(AppNavKey.Media(id)) },
            onOpenThread = { push(AppNavKey.Comment(it)) },
            onBackClick = onBack,
        )
        is AppNavKey.Episodes -> EpisodesHost(onBackClick = onBack)
        is AppNavKey.Search -> SearchHost(
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onOpenCharacter = { push(AppNavKey.Character(it)) },
            onOpenStaff = { push(AppNavKey.Staff(it)) },
            onOpenStudio = { push(AppNavKey.Studio(it)) },
            onOpenUser = { push(AppNavKey.Profile(it)) },
        )
        is AppNavKey.Reviews -> ReviewsHost(
            onOpenUser = { push(AppNavKey.Profile(it)) },
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onBackClick = onBack,
        )
        is AppNavKey.Messages -> MessagesHost(
            userId = route.userId,
            push = push,
            onOpenDrawer = onOpenDrawer,
            onOpenProfile = { push(AppNavKey.Profile(it)) },
        )
        is AppNavKey.SharedContent -> SharedContentHost(
            sharedText = route.text,
            subject = route.subject,
            onClose = onBack,
        )
        AppNavKey.DiscoverRoot, is AppNavKey.DiscoverResults -> DiscoverHost(
            filters = (route as? AppNavKey.DiscoverResults)?.filters,
            isAuthenticated = isAuthenticated,
            onOpenDrawer = onOpenDrawer,
            push = push,
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
        )
        AppNavKey.LibraryRoot -> LibraryHost(
            isAuthenticated = isAuthenticated,
            onOpenDrawer = onOpenDrawer,
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            push = push,
        )
        AppNavKey.SocialRoot -> SocialHost(
            isAuthenticated = isAuthenticated,
            onOpenDrawer = onOpenDrawer,
            push = push,
            onOpenProfile = { push(AppNavKey.Profile(it)) },
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onOpenComments = { push(AppNavKey.Comment(it)) },
            onOpenNotifications = { push(AppNavKey.Notifications) },
        )
        is AppNavKey.Media -> MediaHost(
            mediaId = route.mediaId,
            mediaType = route.mediaType,
            isAuthenticated = isAuthenticated,
            push = push,
            onBackClick = onBack,
        )
        is AppNavKey.MediaCollection -> MediaCollectionHost(
            mediaId = route.mediaId,
            mediaType = route.mediaType,
            section = com.mxt.anitrend.view.fragment.detail.MediaSection.valueOf(route.section.name),
            push = push,
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onBackClick = onBack,
        )
        is AppNavKey.Character -> CharacterHost(
            characterId = route.characterId,
            push = push,
            onBackClick = onBack,
        )
        is AppNavKey.Staff -> StaffHost(
            staffId = route.staffId,
            push = push,
            onBackClick = onBack,
        )
        is AppNavKey.Studio -> StudioHost(
            studioId = route.studioId,
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onBackClick = onBack,
        )
        is AppNavKey.Profile -> ProfileHost(
            userId = route.userId,
            userName = route.userName,
            isAuthenticated = isAuthenticated,
            push = push,
            onBackClick = onBack,
        )
        is AppNavKey.UserMediaList -> UserMediaListHost(
            userId = route.userId,
            userName = route.userName,
            mediaType = route.mediaType,
            push = push,
            onOpenDrawer = onOpenDrawer,
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onBackClick = onBack,
        )
        is AppNavKey.Favourites -> FavouritesHost(
            userId = route.userId,
            userName = route.userName,
            onOpenMedia = { id, type -> push(AppNavKey.Media(id, type)) },
            onBackClick = onBack,
        )
        is AppNavKey.Comment -> CommentHost(
            feedId = route.feedId,
            isAuthenticated = isAuthenticated,
            onOpenProfile = { push(AppNavKey.Profile(it)) },
            onBackClick = onBack,
        )
    }
}

@Composable
private fun SettingsHost(
    onOpenCategory: (String) -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenLogs: () -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val isFirebaseVisible = remember { FirebaseApp.getApps(context).isNotEmpty() }
    com.mxt.anitrend.ui.settings.SettingsScreen(
        isFirebaseVisible = isFirebaseVisible,
        onOpenCategory = onOpenCategory,
        onOpenChangelog = onOpenChangelog,
        onOpenLogs = onOpenLogs,
        onBackClick = onBackClick,
    )
}

@Composable
private fun SettingsCategoryHost(
    categoryId: String,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember { Settings(context) }
    val scheduler: com.mxt.anitrend.util.JobSchedulerUtil = koinInject()
    val syncFrequencyKey = context.getString(R.string.pref_key_sync_frequency)
    val notificationKey = context.getString(R.string.pref_key_new_message_notifications)

    com.mxt.anitrend.ui.settings.SettingsCategoryScreen(
        categoryId = categoryId,
        settings = settings,
        isFirebaseVisible = remember { FirebaseApp.getApps(context).isNotEmpty() },
        isUpdateChannelVisible = remember { context.resources.getBoolean(R.bool.display_update_channel_pref) },
        onPreferenceChanged = { key ->
            when (key) {
                syncFrequencyKey -> {
                    scheduler.cancelTagJob(context)
                    scheduler.scheduleTagJob(context)
                    scheduler.cancelGenreJob(context)
                    scheduler.scheduleGenreJob(context)
                    scheduler.cancelNotificationJob(context)
                    scheduler.scheduleNotificationJob(context)
                }
                notificationKey -> {
                    if (settings.isNotificationEnabled) {
                        scheduler.scheduleNotificationJob(context)
                    } else {
                        scheduler.cancelNotificationJob(context)
                    }
                }
            }
        },
        onBackClick = onBackClick,
    )
}

@Composable
private fun AboutHost(
    onOpenChangelog: () -> Unit,
    onBackClick: () -> Unit,
) {
    AboutScreen(
        versionName = BuildConfig.versionName,
        onOpenChangelog = onOpenChangelog,
        onBackClick = onBackClick,
    )
}

@Composable
private fun ChangelogHost(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ChangelogViewModel = koinViewModel()
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { createMarkwon(context, linkHandler) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChangelogScreen(
        state = state,
        markwon = markwon,
        onLinkClick = linkHandler,
        onRetry = viewModel::retry,
        onBackClick = onBackClick,
    )
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}

@Composable
private fun LoggingHost(onBackClick: () -> Unit) {
    val viewModel: LoggingViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.load() }

    LoggingScreen(
        state = state,
        versionLabel = "v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
        deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL}",
        androidLabel = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        onFilterSelected = viewModel::setFilter,
        onSaveLog = {
            scope.launch {
                val result = viewModel.saveToDownloads()
                val messageRes = if (result.isSuccess) R.string.completed_success else R.string.text_error_request
                Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
            }
        },
        onShareLog = {
            scope.launch {
                runCatching {
                    val shareFile = viewModel.buildShareFile()
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", shareFile)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        clipData = ClipData.newUri(context.contentResolver, context.getString(R.string.drawer_title_share), uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, context.getString(R.string.drawer_title_share)),
                    )
                }.onFailure {
                    Toast.makeText(context, context.getString(R.string.text_error_request), Toast.LENGTH_SHORT).show()
                }
            }
        },
        onClear = viewModel::clear,
        onBackClick = onBackClick,
    )
}

@Composable
private fun NotificationsHost(
    onOpenActivity: (Long) -> Unit,
    onOpenMedia: (Long) -> Unit,
    onOpenThread: (Long) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: NotificationViewModel = koinViewModel()
    val userRepository: com.mxt.anitrend.repository.UserRepository = koinInject()

    LaunchedEffect(Unit) { viewModel.load(page = 0) }

    // The unread badge mirrors the server-side counter on the cached
    // current user; zero for guests and unloaded states.
    val unreadCount = userRepository.cachedCurrentUser?.unreadNotificationCount ?: 0

    com.mxt.anitrend.ui.notification.NotificationsScreen(
        viewModel = viewModel,
        unreadCount = unreadCount,
        onOpenActivity = onOpenActivity,
        onOpenMedia = onOpenMedia,
        onOpenThread = onOpenThread,
        onMarkAllRead = { viewModel.load(page = 0) },
        onBackClick = onBackClick,
    )
}

@Composable
private fun EpisodesHost(onBackClick: () -> Unit) {
    val viewModel: EpisodesViewModel = koinViewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.load(popular = false) }

    com.mxt.anitrend.ui.episode.EpisodesScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        onEpisodeClick = { episode ->
            // Rows carry an external episode link; open it the way the
            // legacy feed did, and fall back silently when absent.
            episode.link?.let { link ->
                context.startActivity(
                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link)),
                )
            }
        },
        onEpisodeLongClick = { episode ->
            episode.link?.let { link ->
                val share = android.content.Intent(android.content.Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(android.content.Intent.EXTRA_TEXT, link)
                context.startActivity(
                    android.content.Intent.createChooser(share, context.getString(com.mxt.anitrend.R.string.drawer_title_share)),
                )
            }
        },
    )
}

@Composable
private fun SearchHost(
    onOpenMedia: (Long, String?) -> Unit,
    onOpenCharacter: (Long) -> Unit,
    onOpenStaff: (Long) -> Unit,
    onOpenStudio: (Long) -> Unit,
    onOpenUser: (Long) -> Unit,
) {
    com.mxt.anitrend.ui.search.SearchScreen(
        mediaViewModel = koinViewModel(),
        characterViewModel = koinViewModel(),
        staffViewModel = koinViewModel(),
        studioViewModel = koinViewModel(),
        userViewModel = koinViewModel(),
        onOpenMedia = onOpenMedia,
        onOpenCharacter = onOpenCharacter,
        onOpenStaff = onOpenStaff,
        onOpenStudio = onOpenStudio,
        onOpenUser = onOpenUser,
    )
}

@Composable
private fun ReviewsHost(
    onOpenUser: (Long) -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.BrowseReviewViewModel = koinViewModel()
    var activeReview by remember { mutableStateOf<ReviewRecord?>(null) }

    LaunchedEffect(Unit) { viewModel.load(type = null, page = 0, sort = null) }

    com.mxt.anitrend.ui.review.ReviewsScreen(
        viewModel = viewModel,
        onOpenReview = { activeReview = it },
        onOpenUser = onOpenUser,
        onBackClick = onBackClick,
    )

    activeReview?.let { review ->
        ReviewReaderSheet(
            review = review,
            onDismiss = { activeReview = null },
            onUserClick = onOpenUser,
            onMediaClick = onOpenMedia,
            onRate = { rating -> viewModel.rateReview(review.id, rating) },
        )
    }
}

@Composable
private fun MessagesHost(
    userId: Long?,
    push: (AppNavKey) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenProfile: (Long) -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MessageFeedViewModel = koinViewModel()
    val userRepository: com.mxt.anitrend.repository.UserRepository = koinInject()
    // A null userId (drawer Messages row) means "the signed-in user's
    // inbox"; guest stays 0 only for the guest render path.
    val ownerId = userId ?: userRepository.cachedCurrentUser?.id ?: 0L

    LaunchedEffect(ownerId) {
        viewModel.load(
            userId = ownerId,
            page = 0,
            pageLimit = 21,
            messageType = com.mxt.anitrend.util.KeyUtil.MESSAGE_TYPE_INBOX,
        )
    }

    com.mxt.anitrend.ui.messages.MessagesScreen(
        viewModel = viewModel,
        ownerId = ownerId,
        onMenuClick = onOpenDrawer,
        onSearchClick = { push(AppNavKey.Search()) },
        // Plan 6.5: a new message starts at recipient selection in the
        // authenticated user search; the composer opens from the recipient.
        onNewMessageClick = { push(AppNavKey.Search()) },
        onOpenProfile = onOpenProfile,
    )
}

@Composable
private fun SharedContentHost(
    sharedText: String?,
    subject: String?,
    onClose: () -> Unit,
) {
    val saveFeedInteractor: SaveFeedInteractor = koinInject()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    SharedContentScreen(
        sharedText = sharedText,
        subject = subject,
        onSubmit = { text ->
            // Saving posts through the canonical mutation interactor; the
            // result surfaces through the feed store, not a local toast.
            scope.launch {
                saveFeedInteractor(SaveFeedRequest.Text(text = text))
            }
        },
        onClose = onClose,
    )
}

@Composable
private fun DiscoverHost(
    filters: com.mxt.anitrend.navigation.BrowseFilters?,
    isAuthenticated: Boolean,
    onOpenDrawer: () -> Unit,
    push: (AppNavKey) -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaBrowseViewModel = koinViewModel()
    val initialType = filters?.mediaType
    val initialFilters =
        filters?.let { f ->
            com.mxt.anitrend.ui.discover.DiscoverFilters(
                sort = f.sort,
                season = f.season,
                format = f.format,
                seasonYear = f.seasonYear,
                startDateLike = f.startDateLike,
                status = f.status,
                genres = f.genres,
                tags = f.tags,
                isAdult = f.isAdult,
            )
        } ?: com.mxt.anitrend.ui.discover.DiscoverFilters()
    val context = LocalContext.current
    var filterSheetOpen by remember { mutableStateOf(false) }
    val formatOptions =
        remember {
            com.mxt.anitrend.graphql.generated.MediaFormat.values().filter { format ->
                com.mxt.anitrend.ui.format.mediaFormatLabelRes(format.name) != R.string.label_unknown
            }
        }
    val formatLabels =
        remember(context, formatOptions) {
            formatOptions.map { format -> context.getString(com.mxt.anitrend.ui.format.mediaFormatLabelRes(format.name)) }
        }

    com.mxt.anitrend.ui.discover.DiscoverScreen(
        viewModel = viewModel,
        initialType = initialType,
        allowAllTypes = true,
        isGuest = !isAuthenticated,
        initialFilters = initialFilters,
        onMenuClick = onOpenDrawer,
        onSearchClick = { push(AppNavKey.Search()) },
        onFiltersClick = { filterSheetOpen = true },
        onMediaClick = onOpenMedia,
        onMediaLongClick = { _, _ -> },
    )

    if (filterSheetOpen) {
        val selectedFormatIndex = formatOptions.indexOfFirst { it.name == filters?.format }
        FilterOptionSheet(
            title = context.getString(R.string.menu_title_formats),
            options = formatLabels,
            initialSelection = if (selectedFormatIndex >= 0) listOf(selectedFormatIndex) else emptyList(),
            isMultiSelect = false,
            onApply = { _, selectedIndices ->
                val format = selectedIndices.firstOrNull()?.let { formatOptions.getOrNull(it)?.name }
                val baseFilters = filters ?: com.mxt.anitrend.navigation.BrowseFilters(mediaType = initialType)
                filterSheetOpen = false
                push(
                    AppNavKey.DiscoverResults(
                        baseFilters.copy(
                            mediaType = baseFilters.mediaType ?: initialType,
                            format = format,
                        ),
                    ),
                )
            },
            onDismiss = { filterSheetOpen = false },
        )
    }
}

@Composable
private fun LibraryHost(
    isAuthenticated: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
    push: (AppNavKey) -> Unit,
) {
    val viewModel: MediaListViewModel = koinViewModel()
    val mutationViewModel: MediaListMutationViewModel = koinViewModel()
    val mediaViewModel: com.mxt.anitrend.viewmodel.MediaViewModel = koinViewModel()
    val userRepository: com.mxt.anitrend.repository.UserRepository = koinInject()
    val settings: Settings = koinInject()
    val listState by viewModel.state.collectAsStateWithLifecycle()
    val userId = userRepository.cachedCurrentUser?.id ?: 0L
    val userName = userRepository.cachedCurrentUser?.name
    val isCurrentUser = viewModel.isCurrentUser(userId, userName)
    var manageTarget by remember { mutableStateOf<ManageListSheetTarget?>(null) }

    com.mxt.anitrend.ui.library.LibraryScreen(
        viewModel = viewModel,
        userId = userId,
        userName = userName,
        isGuest = !isAuthenticated,
        onMenuClick = onOpenDrawer,
        onSearchClick = { push(AppNavKey.Search()) },
        onSortClick = { toggleMediaListSortOrder(settings, viewModel) },
        onEpisodesClick = { push(AppNavKey.Episodes()) },
        onSignInClick = onOpenDrawer,
        onEntryClick = { entry -> onOpenMedia(entry.id, entry.mediaType) },
        onManageClick = { item ->
            val record =
                (listState as? MediaListViewModel.UiState.Success)
                    ?.entries
                    ?.firstOrNull { it.id == item.id }
            if (record != null && isCurrentUser) {
                manageTarget =
                    ManageListSheetTarget(
                        title = item.mediaTitle,
                        mediaId = item.mediaId,
                        mediaType = item.mediaType ?: record.media?.type,
                        record = record,
                        isFavourite = item.mediaIsFavourite,
                        canDelete = record.id > 0L,
                        canFollow = isAuthenticated && userRepository.cachedCurrentUser != null,
                    )
            }
        },
        onIncrement = { item ->
            if (item.canIncrement && isCurrentUser) {
                val record =
                    (listState as? MediaListViewModel.UiState.Success)
                        ?.entries
                        ?.firstOrNull { it.id == item.id }
                record?.let { mutationViewModel.increment(buildIncrementMediaProgressCommand(it)) }
            }
        },
    )

    manageTarget?.let { target ->
        ManageListSheetHost(
            target = target,
            mutationViewModel = mutationViewModel,
            onFollowToggle = { mediaViewModel.toggleFavouriteMedia(target.mediaId, target.mediaType) },
            onDismiss = { manageTarget = null },
        )
    }
}

@Composable
private fun SocialHost(
    isAuthenticated: Boolean,
    onOpenDrawer: () -> Unit,
    push: (AppNavKey) -> Unit,
    onOpenProfile: (Long) -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
    onOpenComments: (Long) -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.FeedListViewModel = koinViewModel()
    val feedState by viewModel.state.collectAsStateWithLifecycle()
    var activeLikes by remember { mutableStateOf<List<UserSummaryRecord>?>(null) }
    fun feedItem(feedId: Long) = (feedState as? com.mxt.anitrend.viewmodel.FeedListViewModel.UiState.Success)
        ?.items
        ?.firstOrNull { it.id == feedId }

    com.mxt.anitrend.ui.feed.FeedScreen(
        viewModel = viewModel,
        isGuest = !isAuthenticated,
        notificationCount = 0,
        onMenuClick = onOpenDrawer,
        onSearchClick = { push(AppNavKey.Search()) },
        onNotificationsClick = onOpenNotifications,
        onNewPostClick = { push(AppNavKey.SharedContent()) },
        onOpenProfile = { feedId ->
            feedItem(feedId)?.let { item ->
                (item.userId ?: item.messengerId)?.let(onOpenProfile)
            }
        },
        onOpenMedia = { feedId, _ ->
            feedItem(feedId)?.let { item ->
                item.mediaId?.let { mediaId -> onOpenMedia(mediaId, item.mediaType) }
            }
        },
        onOpenComments = onOpenComments,
        onShowLikes = { feedId -> activeLikes = feedItem(feedId)?.likes },
    )

    activeLikes?.let { users ->
        FeedLikesSheet(
            users = users,
            onDismiss = { activeLikes = null },
            onUserClick = onOpenProfile,
        )
    }
}

@Composable
private fun MediaHost(
    mediaId: Long,
    mediaType: String?,
    isAuthenticated: Boolean,
    push: (AppNavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    val mediaViewModel: com.mxt.anitrend.viewmodel.MediaViewModel = koinViewModel()
    val overviewViewModel: com.mxt.anitrend.viewmodel.MediaOverviewViewModel = koinViewModel()
    val mediaListViewModel: MediaListViewModel = koinViewModel()
    val mutationViewModel: MediaListMutationViewModel = koinViewModel()
    val userRepository: com.mxt.anitrend.repository.UserRepository = koinInject()
    val settings: com.mxt.anitrend.util.Settings = koinInject()
    val context = androidx.compose.ui.platform.LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    var pendingManageMedia by remember { mutableStateOf(false) }
    var manageTarget by remember { mutableStateOf<ManageListSheetTarget?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { com.mxt.anitrend.ui.markdown.createMarkwon(context, linkHandler) }

    LaunchedEffect(mediaId) {
        mediaViewModel.load(mediaId, mediaType, settings.displayAdultContent)
        overviewViewModel.load(mediaId, mediaType)
    }

    val mediaState by mediaViewModel.state.collectAsStateWithLifecycle()
    val overviewState by overviewViewModel.state.collectAsStateWithLifecycle()
    val displayData by overviewViewModel.displayData.collectAsStateWithLifecycle()
    val listState by mediaListViewModel.state.collectAsStateWithLifecycle()

    when (val current = mediaState) {
        is com.mxt.anitrend.viewmodel.MediaViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
        is com.mxt.anitrend.viewmodel.MediaViewModel.UiState.Error -> com.mxt.anitrend.ui.state.ErrorState(message = current.message, onRetry = { mediaViewModel.load(mediaId, mediaType, settings.displayAdultContent) })
        is com.mxt.anitrend.viewmodel.MediaViewModel.UiState.Success -> {
            val media = current.media
            val overview = (overviewState as? com.mxt.anitrend.viewmodel.MediaOverviewViewModel.UiState.Success)?.record

            LaunchedEffect(pendingManageMedia, listState, mediaId, media) {
                if (pendingManageMedia) {
                    when (val currentList = listState) {
                        is MediaListViewModel.UiState.Success -> {
                            pendingManageMedia = false
                            val record = currentList.entries.firstOrNull { it.mediaId == mediaId }
                            if (record != null) {
                                manageTarget =
                                    ManageListSheetTarget(
                                        title = media.titleUserPreferred.orEmpty().ifBlank { context.getString(R.string.label_unknown) },
                                        mediaId = mediaId,
                                        mediaType = media.type,
                                        record = record,
                                        isFavourite = media.isFavourite,
                                        canDelete = record.id > 0L,
                                        canFollow = isAuthenticated && userRepository.cachedCurrentUser != null,
                                    )
                            } else {
                                Toast.makeText(context, context.getString(R.string.text_error_request), Toast.LENGTH_SHORT).show()
                            }
                        }
                        is MediaListViewModel.UiState.Error -> {
                            pendingManageMedia = false
                            Toast.makeText(context, currentList.message, Toast.LENGTH_SHORT).show()
                        }
                        MediaListViewModel.UiState.Loading -> Unit
                    }
                }
            }

            com.mxt.anitrend.ui.media.MediaDetailScreen(
                mediaId = mediaId,
                title = media.titleUserPreferred,
                bannerImage = media.bannerImage,
                coverImage = overview?.coverImage?.extraLarge ?: overview?.coverImage?.large,
                isFavourite = media.isFavourite,
                isOnList = media.mediaListEntry != null,
                overview = overview,
                display = displayData,
                synopsis = overview?.description,
                isAuthenticated = isAuthenticated,
                markwon = markwon,
                onLinkClick = linkHandler,
                onBackClick = onBackClick,
                onFavouriteClick = { mediaViewModel.toggleFavouriteMedia(mediaId, mediaType) },
                onShareClick = {
                    val shareText =
                        listOfNotNull(
                            media.titleUserPreferred?.takeIf { it.isNotBlank() },
                            media.siteUrl?.takeIf { it.isNotBlank() },
                        ).joinToString("\n")
                    if (shareText.isNotBlank()) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, context.getString(R.string.drawer_title_share)),
                        )
                    }
                },
                onManageClick = {
                    if (isAuthenticated) {
                        val record =
                            (listState as? MediaListViewModel.UiState.Success)
                                ?.entries
                                ?.firstOrNull { it.mediaId == mediaId }
                        when {
                            record != null -> {
                                manageTarget =
                                    ManageListSheetTarget(
                                        title = media.titleUserPreferred.orEmpty().ifBlank { context.getString(R.string.label_unknown) },
                                        mediaId = mediaId,
                                        mediaType = media.type,
                                        record = record,
                                        isFavourite = media.isFavourite,
                                        canDelete = record.id > 0L,
                                        canFollow = userRepository.cachedCurrentUser != null,
                                    )
                            }
                            media.mediaListEntry == null -> {
                                manageTarget =
                                    ManageListSheetTarget(
                                        title = media.titleUserPreferred.orEmpty().ifBlank { context.getString(R.string.label_unknown) },
                                        mediaId = mediaId,
                                        mediaType = media.type,
                                        record = null,
                                        isFavourite = media.isFavourite,
                                        canDelete = false,
                                        canFollow = userRepository.cachedCurrentUser != null,
                                    )
                            }
                            else -> {
                                val user = userRepository.cachedCurrentUser
                                if (user != null) {
                                    pendingManageMedia = true
                                    mediaListViewModel.load(
                                        userId = user.id,
                                        userName = user.name,
                                        mediaType = media.type,
                                        statusIn = null,
                                        preset =
                                        if (media.type == KeyUtil.MANGA) {
                                            com.mxt.anitrend.ui.library.LibraryPreset.MY_MANGA
                                        } else {
                                            com.mxt.anitrend.ui.library.LibraryPreset.MY_ANIME
                                        },
                                    )
                                } else {
                                    Toast.makeText(context, context.getString(R.string.text_error_request), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                onSeeAll = { section ->
                    push(AppNavKey.MediaCollection(mediaId, mediaType, com.mxt.anitrend.navigation.MediaSectionKey.valueOf(section.name)))
                },
            )

            manageTarget?.let { target ->
                ManageListSheetHost(
                    target = target,
                    mutationViewModel = mutationViewModel,
                    onFollowToggle = { mediaViewModel.toggleFavouriteMedia(mediaId, media.type) },
                    onDismiss = { manageTarget = null },
                )
            }
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}

@Composable
private fun MediaCollectionHost(
    mediaId: Long,
    mediaType: String?,
    section: com.mxt.anitrend.view.fragment.detail.MediaSection,
    push: (AppNavKey) -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val settings: com.mxt.anitrend.util.Settings = koinInject()
    val isAdult: Boolean? = if (settings.displayAdultContent) null else false

    when (section) {
        com.mxt.anitrend.view.fragment.detail.MediaSection.RELATIONS -> MediaRelationsHost(mediaId, mediaType, isAdult, onOpenMedia, onBackClick)
        com.mxt.anitrend.view.fragment.detail.MediaSection.RECOMMENDATIONS -> MediaRecommendationsHost(mediaId, mediaType, isAdult, onOpenMedia, onBackClick)
        com.mxt.anitrend.view.fragment.detail.MediaSection.STATS -> MediaStatsHost(mediaId, mediaType, isAdult, onBackClick)
        com.mxt.anitrend.view.fragment.detail.MediaSection.CHARACTERS -> MediaCharactersHost(mediaId, mediaType, isAdult, onOpenMedia, onBackClick)
        com.mxt.anitrend.view.fragment.detail.MediaSection.STAFF -> MediaStaffHost(mediaId, mediaType, isAdult, onOpenMedia, onBackClick)
        com.mxt.anitrend.view.fragment.detail.MediaSection.FEED -> MediaFeedHost(mediaId, push, onBackClick)
        com.mxt.anitrend.view.fragment.detail.MediaSection.REVIEWS ->
            MediaReviewsHost(
                mediaId = mediaId,
                mediaType = mediaType,
                onOpenMedia = onOpenMedia,
                onOpenUser = { userId -> push(AppNavKey.Profile(userId)) },
                onBackClick = onBackClick,
            )
        com.mxt.anitrend.view.fragment.detail.MediaSection.OVERVIEW -> MediaOverviewCollectionHost(mediaId, onBackClick)
    }
}

@Composable
private fun MediaOverviewCollectionHost(mediaId: Long, onBackClick: () -> Unit) {
    val overviewViewModel: com.mxt.anitrend.viewmodel.MediaOverviewViewModel = koinViewModel()
    LaunchedEffect(mediaId) { overviewViewModel.load(mediaId, null) }
    val state by overviewViewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        is com.mxt.anitrend.viewmodel.MediaOverviewViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
        is com.mxt.anitrend.viewmodel.MediaOverviewViewModel.UiState.Error ->
            com.mxt.anitrend.ui.state.ErrorState(
                message = current.message,
                onRetry = { overviewViewModel.load(mediaId, null) },
            )
        is com.mxt.anitrend.viewmodel.MediaOverviewViewModel.UiState.Success -> {
            val record = current.record
            Column(
                modifier =
                androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            ) {
                androidx.compose.material3.Text(
                    text = record.titleUserPreferred.orEmpty(),
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                )
                androidx.compose.material3.Text(
                    text = record.description.orEmpty(),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun MediaRelationsHost(
    mediaId: Long,
    mediaType: String?,
    isAdult: Boolean?,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaRelationViewModel = koinViewModel()
    val type = mediaType?.let { runCatching { com.mxt.anitrend.graphql.generated.MediaType.valueOf(it) }.getOrNull() }
    LaunchedEffect(mediaId) { viewModel.load(mediaId, type, isAdult) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val media =
        (state as? com.mxt.anitrend.viewmodel.MediaRelationViewModel.UiState.Success)
            ?.content
            ?.connection
            ?.edges
            ?.mapNotNull { it.node }
            ?: emptyList()
    val isLoading = state is com.mxt.anitrend.viewmodel.MediaRelationViewModel.UiState.Loading
    val errorMessage = (state as? com.mxt.anitrend.viewmodel.MediaRelationViewModel.UiState.Error)?.message
    com.mxt.anitrend.ui.media.PagedMediaGridScreen(
        media = media,
        isLoading = isLoading,
        errorMessage = errorMessage,
        canLoadMore = false,
        onMediaClick = { onOpenMedia(it.id, it.type) },
        onMediaLongClick = { },
        onLoadMore = { },
        onRetry = { viewModel.load(mediaId, type, isAdult) },
        onBackClick = onBackClick,
    )
}

@Composable
private fun MediaRecommendationsHost(
    mediaId: Long,
    mediaType: String?,
    isAdult: Boolean?,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel = koinViewModel()
    val type = mediaType?.let { runCatching { com.mxt.anitrend.graphql.generated.MediaType.valueOf(it) }.getOrNull() }
    LaunchedEffect(mediaId) { viewModel.load(mediaId, type, isAdult) }
    val items = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        Row(
            modifier =
            androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            androidx.compose.material3.TextButton(
                onClick = onBackClick,
                modifier = androidx.compose.ui.Modifier.heightIn(min = 48.dp),
            ) {
                androidx.compose.material3.Text(
                    text = androidx.compose.ui.res.stringResource(com.mxt.anitrend.R.string.action_back),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                )
            }
        }
        when {
            items.loadState.refresh is androidx.paging.LoadState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
            items.loadState.refresh is androidx.paging.LoadState.Error ->
                com.mxt.anitrend.ui.state.ErrorState(
                    message = (items.loadState.refresh as androidx.paging.LoadState.Error).error.message ?: "",
                    onRetry = { viewModel.load(mediaId, type, isAdult) },
                )
            items.itemCount == 0 ->
                com.mxt.anitrend.ui.state.EmptyState(
                    message = androidx.compose.ui.res.stringResource(com.mxt.anitrend.R.string.layout_empty_response),
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                )
            else ->
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                ) {
                    items(items.itemCount) { index ->
                        val item = items[index] ?: return@items
                        com.mxt.anitrend.ui.media.MediaGridTile(
                            imageUrl = item.coverImage,
                            title = item.title,
                            subtitle = item.mediaType,
                            contentDescription = item.title,
                            onClick = { onOpenMedia(item.mediaId, item.mediaType) },
                            onLongClick = { },
                        )
                    }
                }
        }
    }
}

@Composable
private fun MediaStatsHost(
    mediaId: Long,
    mediaType: String?,
    isAdult: Boolean?,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaStatsViewModel = koinViewModel()
    val type = mediaType?.let { runCatching { com.mxt.anitrend.graphql.generated.MediaType.valueOf(it) }.getOrNull() }
    LaunchedEffect(mediaId) { viewModel.load(mediaId, type, isAdult) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        Row(
            modifier =
            androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            androidx.compose.material3.TextButton(
                onClick = onBackClick,
                modifier = androidx.compose.ui.Modifier.heightIn(min = 48.dp),
            ) {
                androidx.compose.material3.Text(
                    text = androidx.compose.ui.res.stringResource(com.mxt.anitrend.R.string.action_back),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                )
            }
        }
        when (val current = state) {
            is com.mxt.anitrend.viewmodel.MediaStatsViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
            is com.mxt.anitrend.viewmodel.MediaStatsViewModel.UiState.Error ->
                com.mxt.anitrend.ui.state.ErrorState(message = current.message, onRetry = { viewModel.load(mediaId, type, isAdult) })
            is com.mxt.anitrend.viewmodel.MediaStatsViewModel.UiState.Success -> {
                val stats = current.media.stats
                Column(
                    modifier =
                    androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                ) {
                    val scoreRows = stats?.scoreDistribution.orEmpty()
                    if (scoreRows.isEmpty()) {
                        androidx.compose.material3.Text(
                            text = androidx.compose.ui.res.stringResource(com.mxt.anitrend.R.string.layout_empty_response),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        scoreRows.forEach { distribution ->
                            androidx.compose.material3.Text(
                                text = "${distribution.score}: ${distribution.amount}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaCharactersHost(
    mediaId: Long,
    mediaType: String?,
    isAdult: Boolean?,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel = koinViewModel()
    LaunchedEffect(mediaId) { viewModel.load(mediaId, null, 1) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val connection =
        (state as? com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel.UiState.Success)
            ?.content
            ?.connection
    val media = connection?.edges?.mapNotNull { it.node } ?: emptyList()
    val pageInfo = connection?.takeIf { it.hasPageInfo() }?.pageInfo
    val canLoadMore = pageInfo?.hasNextPage() == true
    val nextPage = (pageInfo?.currentPage ?: 1) + 1

    com.mxt.anitrend.ui.media.PagedMediaGridScreen(
        media = media,
        isLoading = state is com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel.UiState.Loading,
        errorMessage = (state as? com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel.UiState.Error)?.message,
        canLoadMore = canLoadMore,
        onMediaClick = { onOpenMedia(it.id, it.type) },
        onMediaLongClick = { },
        onLoadMore = { if (canLoadMore) viewModel.load(mediaId, null, nextPage) },
        onRetry = { viewModel.load(mediaId, null, 1) },
        onBackClick = onBackClick,
    )
}

@Composable
private fun MediaStaffHost(
    mediaId: Long,
    mediaType: String?,
    isAdult: Boolean?,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaStaffRoleViewModel = koinViewModel()
    LaunchedEffect(mediaId) { viewModel.load(mediaId, null, 1) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val connection =
        (state as? com.mxt.anitrend.viewmodel.MediaStaffRoleViewModel.UiState.Success)
            ?.content
            ?.connection
    val media = connection?.edges?.mapNotNull { it.node } ?: emptyList()
    val pageInfo = connection?.takeIf { it.hasPageInfo() }?.pageInfo
    val canLoadMore = pageInfo?.hasNextPage() == true
    val nextPage = (pageInfo?.currentPage ?: 1) + 1

    com.mxt.anitrend.ui.media.PagedMediaGridScreen(
        media = media,
        isLoading = state is com.mxt.anitrend.viewmodel.MediaStaffRoleViewModel.UiState.Loading,
        errorMessage = (state as? com.mxt.anitrend.viewmodel.MediaStaffRoleViewModel.UiState.Error)?.message,
        canLoadMore = canLoadMore,
        onMediaClick = { onOpenMedia(it.id, it.type) },
        onMediaLongClick = { },
        onLoadMore = { if (canLoadMore) viewModel.load(mediaId, null, nextPage) },
        onRetry = { viewModel.load(mediaId, null, 1) },
        onBackClick = onBackClick,
    )
}

@Composable
private fun MediaFeedHost(
    mediaId: Long,
    push: (AppNavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaFeedViewModel = koinViewModel()
    val context = LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    var activeLikes by remember { mutableStateOf<List<UserSummaryRecord>?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { createMarkwon(context, linkHandler) }

    LaunchedEffect(mediaId) {
        viewModel.load(mediaId, isFollowing = false, page = 1, pageLimit = com.mxt.anitrend.util.KeyUtil.PAGING_LIMIT)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        Row(
            modifier =
            androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            androidx.compose.material3.TextButton(
                onClick = onBackClick,
                modifier = androidx.compose.ui.Modifier.heightIn(min = 48.dp),
            ) {
                androidx.compose.material3.Text(
                    text = androidx.compose.ui.res.stringResource(com.mxt.anitrend.R.string.action_back),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                )
            }
        }
        when (val current = state) {
            is com.mxt.anitrend.viewmodel.MediaFeedViewModel.UiState.Loading ->
                com.mxt.anitrend.ui.state.LoadingState(modifier = androidx.compose.ui.Modifier.fillMaxSize())
            is com.mxt.anitrend.viewmodel.MediaFeedViewModel.UiState.Error ->
                com.mxt.anitrend.ui.state.ErrorState(
                    message = current.message,
                    onRetry = {
                        viewModel.load(mediaId, isFollowing = false, page = 1, pageLimit = com.mxt.anitrend.util.KeyUtil.PAGING_LIMIT)
                    },
                )
            is com.mxt.anitrend.viewmodel.MediaFeedViewModel.UiState.Success -> {
                if (current.items.isEmpty()) {
                    com.mxt.anitrend.ui.state.EmptyState(
                        message = androidx.compose.ui.res.stringResource(com.mxt.anitrend.R.string.layout_empty_response),
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    )
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    ) {
                        items(current.items, key = { "media-feed:${it.id}:${it.createdAt}" }) { item ->
                            com.mxt.anitrend.ui.feed.FeedRow(
                                item = item,
                                markwon = markwon,
                                onLinkClick = linkHandler,
                                onOpenProfile = {
                                    (item.userId ?: item.messengerId)?.let { push(AppNavKey.Profile(it)) }
                                },
                                onOpenMedia = {
                                    item.mediaId?.let { push(AppNavKey.Media(it, item.mediaType)) }
                                },
                                onOpenComments = { push(AppNavKey.Comment(item.id)) },
                                onShowLikes = { activeLikes = item.likes },
                                onToggleLike = { viewModel.toggleLike(item.id) },
                                onDelete = { feedId, _ -> viewModel.deleteFeed(feedId) },
                            )
                        }
                    }
                }
            }
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
    activeLikes?.let { users ->
        FeedLikesSheet(
            users = users,
            onDismiss = { activeLikes = null },
            onUserClick = { push(AppNavKey.Profile(it)) },
        )
    }
}

@Composable
private fun MediaReviewsHost(
    mediaId: Long,
    mediaType: String?,
    onOpenMedia: (Long, String?) -> Unit,
    onOpenUser: (Long) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.BrowseReviewViewModel = koinViewModel()
    var activeReview by remember { mutableStateOf<ReviewRecord?>(null) }

    LaunchedEffect(mediaId) { viewModel.load(type = null, page = 0, sort = null) }
    com.mxt.anitrend.ui.review.ReviewsScreen(
        viewModel = viewModel,
        onOpenReview = { activeReview = it },
        onOpenUser = onOpenUser,
        onBackClick = onBackClick,
    )
    activeReview?.let { review ->
        ReviewReaderSheet(
            review = review,
            onDismiss = { activeReview = null },
            onUserClick = onOpenUser,
            onMediaClick = onOpenMedia,
            onRate = { rating -> viewModel.rateReview(review.id, rating) },
        )
    }
}

@Composable
private fun CharacterHost(
    characterId: Long,
    push: (AppNavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.CharacterViewModel = koinViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { com.mxt.anitrend.ui.markdown.createMarkwon(context, linkHandler) }

    LaunchedEffect(characterId) { viewModel.load(characterId) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        is com.mxt.anitrend.viewmodel.CharacterViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
        is com.mxt.anitrend.viewmodel.CharacterViewModel.UiState.Error ->
            com.mxt.anitrend.ui.state.ErrorState(
                message = current.message,
                onRetry = { viewModel.load(characterId) },
            )
        is com.mxt.anitrend.viewmodel.CharacterViewModel.UiState.Success -> {
            val character = current.character
            com.mxt.anitrend.ui.character.CharacterScreen(
                name = character.name,
                nativeName = null,
                alternativeNames = null,
                description = null,
                appearances = emptyList(),
                voiceCast = emptyList(),
                markwon = markwon,
                onLinkClick = linkHandler,
                onSeeAll = { },
                onBackClick = onBackClick,
                onMediaClick = { media -> push(AppNavKey.Media(media.id, media.type)) },
                onStaffClick = { push(AppNavKey.Staff(it)) },
            )
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}

@Composable
private fun StaffHost(
    staffId: Long,
    push: (AppNavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.StaffViewModel = koinViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { com.mxt.anitrend.ui.markdown.createMarkwon(context, linkHandler) }

    LaunchedEffect(staffId) { viewModel.load(staffId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        is com.mxt.anitrend.viewmodel.StaffViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
        is com.mxt.anitrend.viewmodel.StaffViewModel.UiState.Error ->
            com.mxt.anitrend.ui.state.ErrorState(message = current.message, onRetry = { viewModel.load(staffId) })
        is com.mxt.anitrend.viewmodel.StaffViewModel.UiState.Success -> {
            val staff = current.staff
            com.mxt.anitrend.ui.staff.StaffScreen(
                name = staff.name,
                nativeName = null,
                alternativeNames = null,
                language = null,
                description = null,
                voiceActing = emptyList(),
                credits = emptyList(),
                markwon = markwon,
                onLinkClick = linkHandler,
                onSeeAll = { },
                onBackClick = onBackClick,
                onMediaClick = { media -> push(AppNavKey.Media(media.id, media.type)) },
                onCharacterClick = { push(AppNavKey.Character(it)) },
            )
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
}

@Composable
private fun StudioHost(
    studioId: Long,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val studioViewModel: com.mxt.anitrend.viewmodel.StudioViewModel = koinViewModel()
    val mediaViewModel: com.mxt.anitrend.viewmodel.StudioMediaViewModel = koinViewModel()

    LaunchedEffect(studioId) {
        studioViewModel.load(studioId)
        mediaViewModel.load(studioId, page = 1, perPage = 21, sort = null)
    }

    val studioState by studioViewModel.state.collectAsStateWithLifecycle()
    val mediaState by mediaViewModel.state.collectAsStateWithLifecycle()

    val media =
        when (val mediaCurrent = mediaState) {
            is com.mxt.anitrend.viewmodel.StudioMediaViewModel.UiState.Success -> mediaCurrent.container.connection.pageData
            else -> emptyList()
        }

    com.mxt.anitrend.ui.studio.StudioScreen(
        studioName = (studioState as? com.mxt.anitrend.viewmodel.StudioViewModel.UiState.Success)?.studio?.name.orEmpty(),
        studioFavourites = 0,
        isFavourite = (studioState as? com.mxt.anitrend.viewmodel.StudioViewModel.UiState.Success)?.studio?.isFavourite ?: false,
        media = media,
        isLoading = mediaState is com.mxt.anitrend.viewmodel.StudioMediaViewModel.UiState.Loading,
        errorMessage = (mediaState as? com.mxt.anitrend.viewmodel.StudioMediaViewModel.UiState.Error)?.message,
        canLoadMore = false,
        onFavouriteToggle = {
            studioState.let { current ->
                if (current is com.mxt.anitrend.viewmodel.StudioViewModel.UiState.Success) {
                    studioViewModel.toggleFavouriteStudio(studioId)
                }
            }
        },
        onLoadMore = { mediaViewModel.load(studioId, page = media.size / 21 + 1, perPage = 21, sort = null) },
        onRetry = { mediaViewModel.load(studioId, page = 1, perPage = 21, sort = null) },
        onMediaClick = { onOpenMedia(it.id, it.type) },
        onMediaLongClick = { },
        onBackClick = onBackClick,
    )
}

@Composable
private fun ProfileHost(
    userId: Long,
    userName: String?,
    isAuthenticated: Boolean,
    push: (AppNavKey) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.ProfileViewModel = koinViewModel()
    val userListViewModel: UserListViewModel = koinViewModel()
    val userListState by userListViewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var spoilerText by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by rememberSaveable(userId) { mutableStateOf(0) }
    var activeUserList by remember { mutableStateOf<com.mxt.anitrend.ui.profile.ProfileSection?>(null) }
    val linkHandler = rememberMarkdownLinkHandler { spoilerText = it }
    val markwon = remember { com.mxt.anitrend.ui.markdown.createMarkwon(context, linkHandler) }

    LaunchedEffect(userId, userName) { viewModel.load(userId, userName) }

    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        is com.mxt.anitrend.viewmodel.ProfileViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
        is com.mxt.anitrend.viewmodel.ProfileViewModel.UiState.Error ->
            com.mxt.anitrend.ui.state.ErrorState(
                message = current.message,
                onRetry = { viewModel.load(userId, userName) },
            )
        is com.mxt.anitrend.viewmodel.ProfileViewModel.UiState.Success -> {
            val user = current.user
            com.mxt.anitrend.ui.profile.ProfileScreen(
                avatar = com.mxt.anitrend.ui.profile.AvatarIndicatorState(avatarUrl = user.avatar?.large ?: user.avatar?.medium),
                displayName = user.name.orEmpty(),
                // UserBase projection carries no about text; the profile
                // about tab renders empty until a richer record exists.
                about = null,
                stats = null,
                aboutStats = com.mxt.anitrend.ui.profile.AboutPanelStats(followingCount = "0", followersCount = "0", favouritesCount = "0"),
                selectedTabIndex = selectedTabIndex,
                markwon = markwon,
                onLinkClick = linkHandler,
                onAvatarClick = { },
                onTabSelected = { selectedTabIndex = it },
                onAnimeTotalClick = {
                    push(AppNavKey.UserMediaList(userId, userName, KeyUtil.ANIME))
                },
                onAnimeTimeClick = {
                    push(AppNavKey.UserMediaList(userId, userName, KeyUtil.ANIME))
                },
                onMangaTotalClick = {
                    push(AppNavKey.UserMediaList(userId, userName, KeyUtil.MANGA))
                },
                onMangaChaptersClick = {
                    push(AppNavKey.UserMediaList(userId, userName, KeyUtil.MANGA))
                },
                onSeeAll = { section ->
                    when (section) {
                        com.mxt.anitrend.ui.profile.ProfileSection.ABOUT ->
                            push(AppNavKey.Favourites(userId, userName))
                        com.mxt.anitrend.ui.profile.ProfileSection.ANIME ->
                            push(AppNavKey.UserMediaList(userId, userName, KeyUtil.ANIME))
                        com.mxt.anitrend.ui.profile.ProfileSection.MANGA ->
                            push(AppNavKey.UserMediaList(userId, userName, KeyUtil.MANGA))
                        com.mxt.anitrend.ui.profile.ProfileSection.REVIEWS ->
                            push(AppNavKey.Reviews())
                        com.mxt.anitrend.ui.profile.ProfileSection.FOLLOWERS -> {
                            if (userId > 0L) {
                                activeUserList = section
                                userListViewModel.loadFollowers(userId, page = 1, perPage = KeyUtil.PAGING_LIMIT)
                            }
                        }
                        com.mxt.anitrend.ui.profile.ProfileSection.FOLLOWING -> {
                            if (userId > 0L) {
                                activeUserList = section
                                userListViewModel.loadFollowing(userId, page = 1, perPage = KeyUtil.PAGING_LIMIT)
                            }
                        }
                    }
                },
                onBackClick = onBackClick,
            )
        }
    }
    if (spoilerText != null) {
        SpoilerSheet(
            text = spoilerText,
            onDismiss = { spoilerText = null },
        )
    }
    activeUserList?.let { section ->
        UserListSheet(
            titleRes =
            if (section == com.mxt.anitrend.ui.profile.ProfileSection.FOLLOWERS) {
                R.string.followers
            } else {
                R.string.following
            },
            onDismiss = { activeUserList = null },
            rowContent = {
                when (val users = userListState) {
                    UserListViewModel.UiState.Loading -> com.mxt.anitrend.ui.state.LoadingState()
                    is UserListViewModel.UiState.Error ->
                        com.mxt.anitrend.ui.state.ErrorState(
                            message = users.message,
                            onRetry = {
                                if (section == com.mxt.anitrend.ui.profile.ProfileSection.FOLLOWERS) {
                                    userListViewModel.loadFollowers(userId, page = 1, perPage = KeyUtil.PAGING_LIMIT)
                                } else {
                                    userListViewModel.loadFollowing(userId, page = 1, perPage = KeyUtil.PAGING_LIMIT)
                                }
                            },
                        )
                    is UserListViewModel.UiState.Success -> {
                        if (users.container.pageData.isEmpty()) {
                            com.mxt.anitrend.ui.state.EmptyState(
                                message = context.getString(R.string.layout_empty_response),
                            )
                        } else {
                            Column {
                                users.container.pageData.forEach { listUser ->
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            if (listUser.id > 0L) {
                                                push(AppNavKey.Profile(listUser.id, listUser.name))
                                            }
                                        },
                                        enabled = listUser.id > 0L,
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = listUser.name ?: context.getString(R.string.label_unknown),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun UserMediaListHost(
    userId: Long,
    userName: String?,
    mediaType: String?,
    push: (AppNavKey) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: MediaListViewModel = koinViewModel()
    val mutationViewModel: MediaListMutationViewModel = koinViewModel()
    val mediaViewModel: com.mxt.anitrend.viewmodel.MediaViewModel = koinViewModel()
    val settings: Settings = koinInject()
    val listState by viewModel.state.collectAsStateWithLifecycle()
    val isCurrentUser = viewModel.isCurrentUser(userId, userName)
    var manageTarget by remember { mutableStateOf<ManageListSheetTarget?>(null) }

    com.mxt.anitrend.ui.library.LibraryScreen(
        viewModel = viewModel,
        userId = userId,
        userName = userName,
        isGuest = false,
        onMenuClick = onOpenDrawer,
        onSearchClick = { push(AppNavKey.Search()) },
        onSortClick = { toggleMediaListSortOrder(settings, viewModel) },
        onEpisodesClick = { push(AppNavKey.Episodes()) },
        onSignInClick = onOpenDrawer,
        onEntryClick = { entry -> onOpenMedia(entry.id, entry.mediaType) },
        onManageClick = { item ->
            val record =
                (listState as? MediaListViewModel.UiState.Success)
                    ?.entries
                    ?.firstOrNull { it.id == item.id }
            if (record != null && isCurrentUser) {
                manageTarget =
                    ManageListSheetTarget(
                        title = item.mediaTitle,
                        mediaId = item.mediaId,
                        mediaType = item.mediaType ?: record.media?.type,
                        record = record,
                        isFavourite = item.mediaIsFavourite,
                        canDelete = record.id > 0L,
                        canFollow = settings.isAuthenticated,
                    )
            }
        },
        onIncrement = { item ->
            if (item.canIncrement && isCurrentUser) {
                val record =
                    (listState as? MediaListViewModel.UiState.Success)
                        ?.entries
                        ?.firstOrNull { it.id == item.id }
                record?.let { mutationViewModel.increment(buildIncrementMediaProgressCommand(it)) }
            }
        },
        initialPreset =
        if (mediaType == KeyUtil.MANGA) {
            com.mxt.anitrend.ui.library.LibraryPreset.MY_MANGA
        } else {
            com.mxt.anitrend.ui.library.LibraryPreset.MY_ANIME
        },
    )

    manageTarget?.let { target ->
        ManageListSheetHost(
            target = target,
            mutationViewModel = mutationViewModel,
            onFollowToggle = { mediaViewModel.toggleFavouriteMedia(target.mediaId, target.mediaType) },
            onDismiss = { manageTarget = null },
        )
    }
}

@Composable
private fun FavouritesHost(
    userId: Long,
    userName: String?,
    onOpenMedia: (Long, String?) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.MediaFavouritesViewModel = koinViewModel()

    LaunchedEffect(userId) {
        viewModel.load(userId = userId, page = 1, mediaType = KeyUtil.ANIME)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state as? com.mxt.anitrend.viewmodel.MediaFavouritesViewModel.UiState.Success
    val favourites = current?.content?.connection
    val mediaPage =
        when (current?.mediaType) {
            KeyUtil.MANGA -> favourites?.manga
            else -> favourites?.anime
        }
    val media = mediaPage?.pageData.orEmpty()
    val pageInfo = mediaPage?.takeIf { it.hasPageInfo() }?.pageInfo
    val canLoadMore = pageInfo?.hasNextPage() == true
    val peopleCount =
        (favourites?.characters?.pageData?.size ?: 0) +
            (favourites?.staff?.pageData?.size ?: 0) +
            (favourites?.studios?.pageData?.size ?: 0)

    com.mxt.anitrend.ui.favourite.FavouritesScreen(
        media = media,
        peopleCount = peopleCount,
        isLoading = state is com.mxt.anitrend.viewmodel.MediaFavouritesViewModel.UiState.Loading,
        errorMessage = (state as? com.mxt.anitrend.viewmodel.MediaFavouritesViewModel.UiState.Error)?.message,
        canLoadMore = canLoadMore,
        onLoadMore = {
            if (current != null && pageInfo?.hasNextPage() == true) {
                viewModel.load(
                    userId = userId,
                    page = pageInfo.currentPage + 1,
                    mediaType = current.mediaType,
                )
            }
        },
        onRetry = {
            viewModel.load(
                userId = userId,
                page = 1,
                mediaType = current?.mediaType ?: KeyUtil.ANIME,
            )
        },
        onMediaClick = { onOpenMedia(it.id, it.type) },
        onMediaLongClick = { },
        onBackClick = onBackClick,
        peopleRow = { },
    )
}

@Composable
private fun CommentHost(
    feedId: Long,
    isAuthenticated: Boolean,
    onOpenProfile: (Long) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: com.mxt.anitrend.viewmodel.CommentViewModel = koinViewModel()
    val userRepository: com.mxt.anitrend.repository.UserRepository = koinInject()
    val currentUserId = userRepository.cachedCurrentUser?.id

    LaunchedEffect(feedId) { viewModel.load(feedId, currentUserId) }

    com.mxt.anitrend.ui.comments.ThreadScreen(
        viewModel = viewModel,
        feedId = feedId,
        currentUserId = currentUserId,
        isGuest = !isAuthenticated,
        onBackClick = onBackClick,
        onOpenProfile = onOpenProfile,
    )
}
private data class ManageListSheetTarget(
    val title: String,
    val mediaId: Long,
    val mediaType: String?,
    val record: MediaListRecord?,
    val isFavourite: Boolean,
    val canDelete: Boolean,
    val canFollow: Boolean,
)

private fun toggleMediaListSortOrder(settings: Settings, viewModel: MediaListViewModel) {
    settings.sortOrder = if (settings.sortOrder == KeyUtil.ASC) KeyUtil.DESC else KeyUtil.ASC
    viewModel.onSortPreferenceChanged()
}

@Composable
private fun ManageListSheetHost(
    target: ManageListSheetTarget,
    mutationViewModel: MediaListMutationViewModel,
    onFollowToggle: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val committedModel =
        remember(target) {
            target.record?.toMediaList() ?: MediaList().apply { mediaId = target.mediaId }
        }
    val initialDraft = remember(target) { committedModel.toDraft() }
    var draft by remember(target) { mutableStateOf(initialDraft) }
    val mutationFlow =
        remember(mutationViewModel, target.mediaId, target.record?.id) {
            mutationViewModel.observeTarget(target.mediaId, target.record?.id)
        }
    val mutation by mutationFlow.collectAsStateWithLifecycle(
        initialValue = MediaListMutationViewModel.TargetState(),
    )
    var pendingSave by remember(target) { mutableStateOf(false) }
    var pendingDelete by remember(target) { mutableStateOf(false) }
    var sawSaveRunning by remember(target) { mutableStateOf(false) }
    var sawDeleteRunning by remember(target) { mutableStateOf(false) }

    LaunchedEffect(mutation, pendingSave, pendingDelete) {
        if (pendingSave) {
            if (mutation.isSaveRunning) {
                sawSaveRunning = true
            } else if (sawSaveRunning) {
                pendingSave = false
                sawSaveRunning = false
                if (mutation.completedAction == MediaListMutationViewModel.CompletedAction.SAVED) {
                    onDismiss()
                } else {
                    mutation.errorMessage?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        if (pendingDelete) {
            if (mutation.isDeleteRunning) {
                sawDeleteRunning = true
            } else if (sawDeleteRunning) {
                pendingDelete = false
                sawDeleteRunning = false
                if (mutation.completedAction == MediaListMutationViewModel.CompletedAction.DELETED) {
                    onDismiss()
                } else {
                    mutation.errorMessage?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val options = mutationViewModel.currentUserMediaListOptions
    val customLists =
        if (target.mediaType == KeyUtil.MANGA) {
            options?.mangaList?.customLists.orEmpty()
        } else {
            options?.animeList?.customLists.orEmpty()
        }

    ManageListSheet(
        title = target.title,
        isAnime = target.mediaType != KeyUtil.MANGA,
        canDelete = target.canDelete,
        isSaving = mutation.isSaveRunning,
        isDeletePending = mutation.isDeleteRunning,
        canFollow = target.canFollow,
        isFavourite = target.isFavourite,
        draft = draft,
        customLists = customLists,
        onFollowToggle = onFollowToggle,
        onDraftChange = { draft = it },
        onApply = {
            if (!pendingSave && !pendingDelete) {
                pendingSave = true
                sawSaveRunning = false
                mutationViewModel.save(draft.toSaveMediaListEntryCommand(committedModel))
            }
        },
        onReset = { draft = initialDraft },
        onDelete = {
            val record = target.record
            if (!pendingSave && !pendingDelete && target.canDelete && record != null) {
                pendingDelete = true
                sawDeleteRunning = false
                mutationViewModel.delete(record.id, target.mediaId)
            }
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun FeedLikesSheet(
    users: List<UserSummaryRecord>,
    onDismiss: () -> Unit,
    onUserClick: (Long) -> Unit,
) {
    UserListSheet(
        titleRes = R.string.title_view_likes,
        onDismiss = onDismiss,
        rowContent = {
            if (users.isEmpty()) {
                androidx.compose.material3.Text(
                    text = androidx.compose.ui.res.stringResource(R.string.text_no_likes),
                )
            } else {
                Column {
                    users.forEach { user ->
                        androidx.compose.material3.TextButton(
                            onClick = { onUserClick(user.id) },
                            enabled = user.id > 0L,
                        ) {
                            androidx.compose.material3.Text(
                                text = user.name ?: androidx.compose.ui.res.stringResource(R.string.label_unknown),
                            )
                        }
                    }
                }
            }
        },
    )
}
