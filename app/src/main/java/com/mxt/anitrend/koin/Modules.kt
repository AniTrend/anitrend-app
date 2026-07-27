package com.mxt.anitrend.koin

import android.app.NotificationManager
import android.content.Context
import android.webkit.WebSettings
import co.anitrend.retrofit.graphql.converter.GraphConverter
import co.anitrend.retrofit.graphql.model.GraphQLDocumentRegistry
import co.anitrend.retrofit.graphql.serialization.kotlinx.KotlinxGraphQLJson
import co.anitrend.support.markdown.center.CenterPlugin
import co.anitrend.support.markdown.core.CorePlugin
import co.anitrend.support.markdown.ephasis.EmphasisPlugin
import co.anitrend.support.markdown.heading.HeadingPlugin
import co.anitrend.support.markdown.horizontal.HorizontalLinePlugin
import co.anitrend.support.markdown.image.ImagePlugin
import co.anitrend.support.markdown.italics.ItalicsPlugin
import co.anitrend.support.markdown.mention.MentionPlugin
import co.anitrend.support.markdown.spoiler.SpoilerPlugin
import co.anitrend.support.markdown.strike.StrikeThroughPlugin
import co.anitrend.support.markdown.webm.WebMPlugin
import co.anitrend.support.markdown.youtube.YouTubePlugin
import com.bumptech.glide.Glide
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.analytics.AnalyticsLogging
import com.mxt.anitrend.analytics.contract.ISupportAnalytics
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.base.plugin.image.GlideImagePlugin
import com.mxt.anitrend.base.plugin.image.ImageConfigurationPlugin
import com.mxt.anitrend.base.plugin.text.TextConfigurationPlugin
import com.mxt.anitrend.coordinator.WidgetMutationCoordinator
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.logFile
import com.mxt.anitrend.graphql.generated.GeneratedGraphQLRegistry
import com.mxt.anitrend.model.api.converter.AniGraphConverter
import com.mxt.anitrend.model.api.interceptor.AuthInterceptor
import com.mxt.anitrend.model.api.interceptor.CacheInterceptor
import com.mxt.anitrend.model.api.interceptor.ClientInterceptor
import com.mxt.anitrend.model.api.interceptor.NetworkCacheInterceptor
import com.mxt.anitrend.model.api.retro.ServiceFactory
import com.mxt.anitrend.model.api.retro.anilist.BaseService
import com.mxt.anitrend.model.api.retro.anilist.BrowseService
import com.mxt.anitrend.model.api.retro.anilist.CharacterService
import com.mxt.anitrend.model.api.retro.anilist.FeedService
import com.mxt.anitrend.model.api.retro.anilist.MediaService
import com.mxt.anitrend.model.api.retro.anilist.SearchService
import com.mxt.anitrend.model.api.retro.anilist.StaffService
import com.mxt.anitrend.model.api.retro.anilist.StudioService
import com.mxt.anitrend.model.api.retro.anilist.UserService
import com.mxt.anitrend.model.api.retro.base.GiphyService
import com.mxt.anitrend.model.api.retro.base.RepositoryService
import com.mxt.anitrend.model.api.retro.crunchy.EpisodeService
import com.mxt.anitrend.model.entity.MyObjectBox
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.CharacterRepository
import com.mxt.anitrend.repository.CrunchyrollRepository
import com.mxt.anitrend.repository.FeedRepository
import com.mxt.anitrend.repository.MediaRepository
import com.mxt.anitrend.repository.SearchRepository
import com.mxt.anitrend.repository.StaffRepository
import com.mxt.anitrend.repository.StudioRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.CompatUtil.cacheProvider
import com.mxt.anitrend.util.ConfigurationUtil
import com.mxt.anitrend.util.JobSchedulerUtil
import com.mxt.anitrend.util.NotificationUtil
import com.mxt.anitrend.util.Settings
import com.mxt.anitrend.viewmodel.AiringListViewModel
import com.mxt.anitrend.viewmodel.BrowseReviewViewModel
import com.mxt.anitrend.viewmodel.CharacterActorsViewModel
import com.mxt.anitrend.viewmodel.CharacterFavouritesViewModel
import com.mxt.anitrend.viewmodel.CharacterOverviewViewModel
import com.mxt.anitrend.viewmodel.CharacterSearchViewModel
import com.mxt.anitrend.viewmodel.CharacterViewModel
import com.mxt.anitrend.viewmodel.FeedListViewModel
import com.mxt.anitrend.viewmodel.GiphyViewModel
import com.mxt.anitrend.viewmodel.LoggingViewModel
import com.mxt.anitrend.viewmodel.LoginAuthViewModel
import com.mxt.anitrend.viewmodel.LoginUserViewModel
import com.mxt.anitrend.viewmodel.MainViewModel
import com.mxt.anitrend.viewmodel.MediaAnimeRoleViewModel
import com.mxt.anitrend.viewmodel.MediaBrowseViewModel
import com.mxt.anitrend.viewmodel.MediaCharacterViewModel
import com.mxt.anitrend.viewmodel.MediaFavouritesViewModel
import com.mxt.anitrend.viewmodel.MediaFeedViewModel
import com.mxt.anitrend.viewmodel.MediaFormatViewModel
import com.mxt.anitrend.viewmodel.MediaLatestViewModel
import com.mxt.anitrend.viewmodel.MediaListViewModel
import com.mxt.anitrend.viewmodel.MediaOverviewViewModel
import com.mxt.anitrend.viewmodel.MediaRecommendationsViewModel
import com.mxt.anitrend.viewmodel.MediaRelationViewModel
import com.mxt.anitrend.viewmodel.MediaSearchViewModel
import com.mxt.anitrend.viewmodel.MediaStaffRoleViewModel
import com.mxt.anitrend.viewmodel.MediaStaffViewModel
import com.mxt.anitrend.viewmodel.MediaStatsViewModel
import com.mxt.anitrend.viewmodel.MediaViewModel
import com.mxt.anitrend.viewmodel.MessageFeedViewModel
import com.mxt.anitrend.viewmodel.MetadataProvider
import com.mxt.anitrend.viewmodel.NotificationViewModel
import com.mxt.anitrend.viewmodel.ProfileViewModel
import com.mxt.anitrend.viewmodel.ReviewViewModel
import com.mxt.anitrend.viewmodel.StaffFavouritesViewModel
import com.mxt.anitrend.viewmodel.StaffOverviewViewModel
import com.mxt.anitrend.viewmodel.StaffSearchViewModel
import com.mxt.anitrend.viewmodel.StaffViewModel
import com.mxt.anitrend.viewmodel.StudioFavouritesViewModel
import com.mxt.anitrend.viewmodel.StudioMediaViewModel
import com.mxt.anitrend.viewmodel.StudioSearchViewModel
import com.mxt.anitrend.viewmodel.StudioViewModel
import com.mxt.anitrend.viewmodel.SuggestionListViewModel
import com.mxt.anitrend.viewmodel.UserFeedViewModel
import com.mxt.anitrend.viewmodel.UserListViewModel
import com.mxt.anitrend.viewmodel.UserOverviewViewModel
import com.mxt.anitrend.viewmodel.UserSearchViewModel
import com.mxt.anitrend.worker.*
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.wax911.emojify.EmojiManager
import io.wax911.emojify.serializer.gson.GsonDeserializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.onClose
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

private val coroutineModule = module {
    single<CoroutineDispatcher>(DefaultDispatcherQualifier) {
        Dispatchers.Default
    }
    single<CoroutineDispatcher>(MainDispatcherQualifier) {
        Dispatchers.Main
    }
    single<CoroutineDispatcher>(IoDispatcherQualifier) {
        Dispatchers.IO
    }
    single<CoroutineDispatcher>(UnconfinedDispatcherQualifier) {
        Dispatchers.Unconfined
    }
    single<CoroutineScope>(
        qualifier = ApplicationScopeQualifier,
        createdAtStart = true,
    ) {
        CoroutineScope(
            SupervisorJob() +
                get<CoroutineDispatcher>(DefaultDispatcherQualifier) +
                CoroutineName("ApplicationScope"),
        )
    } onClose { scope ->
        scope?.cancel()
    }
}

private val coreModule = module {
    single {
        DatabaseHelper(
            store = MyObjectBox.builder()
                .androidContext(androidContext())
                .build(),
        )
    } bind BoxQuery::class

    single<ISupportAnalytics> {
        AnalyticsLogging(
            context = androidContext(),
            settings = get(),
        )
    }

    single {
        JobSchedulerUtil(
            settings = get(),
        )
    }

    factory {
        Settings(
            context = androidContext(),
        )
    }

    factory {
        ConfigurationUtil(
            settings = get(),
        )
    }

    factory {
        val context = androidContext()
        NotificationUtil(
            context = context,
            settings = get(),
            context.getSystemService(
                Context.NOTIFICATION_SERVICE,
            ) as NotificationManager?,
        )
    }
    single(createdAtStart = true) {
        EmojiManager.create(
            context = androidContext(),
            serializer = GsonDeserializer(),
        )
    }
    single(
        qualifier = named("ua"),
        createdAtStart = true,
    ) {
        WebSettings.getDefaultUserAgent(androidContext())
    }
}

private val widgetModule = module {
    factory {
        Markwon.builder(get())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(CorePlugin.create())
            .usePlugin(HorizontalLinePlugin.create())
            .usePlugin(HeadingPlugin.create())
            .usePlugin(EmphasisPlugin.create())
            .usePlugin(CenterPlugin.create())
            .usePlugin(ImagePlugin.create())
            .usePlugin(WebMPlugin.create())
            .usePlugin(YouTubePlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(SpoilerPlugin.create())
            .usePlugin(MentionPlugin.create())
            .usePlugin(StrikeThroughPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(get<Context>()))
            .usePlugin(ItalicsPlugin.create())
            .usePlugin(
                GlideImagesPlugin.create(
                    GlideImagePlugin.create(
                        Glide.with(get<Context>()),
                        get(named("ua")),
                    ),
                ),
            )
            .usePlugin(ImageConfigurationPlugin.create())
            .usePlugin(TextConfigurationPlugin.create())
            .build()
    }
    single {
        MarkwonEditor.builder(get())
            .useEditHandler(EmphasisEditHandler())
            .useEditHandler(StrongEmphasisEditHandler())
            .build()
    }
}

private val workerModule = module {
    worker { scope ->
        NotificationWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            notificationUtil = get(),
            userRepository = get(),
        )
    }
    worker { scope ->
        ClearNotificationWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            userRepository = get(),
        )
    }
    worker { scope ->
        GenreSyncWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            baseRepository = get(),
        )
    }
    worker { scope ->
        TagSyncWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            baseRepository = get(),
        )
    }
    worker { scope ->
        UpdateWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            repositoryService = get(),
        )
    }
}

private val presenterModule = module {
    factory {
        BasePresenter(context = androidContext(), boxQuery = get(), settings = get())
    }
}

private val networkModule = module {
    single<GraphQLDocumentRegistry> {
        GeneratedGraphQLRegistry
    }
    factory {
        AuthInterceptor(
            settings = get(),
        )
    }
    factory {
        ChuckerInterceptor.Builder(context = androidContext())
            .collector(
                collector = ChuckerCollector(
                    context = androidContext(),
                    showNotification = true,
                    retentionPeriod = RetentionManager.Period.ONE_WEEK,
                ),
            )
            .maxContentLength(
                length = 250000L,
            )
            .redactHeaders(
                headerNames = setOf(BuildConfig.HEADER_KEY),
            )
            .alwaysReadResponseBody(false)
            .build()
    }
    single {
        @Suppress("DEPRECATION")
        val gson = GsonBuilder()
            .enableComplexMapKeySerialization()
            .setLenient()
            .create()
        AniGraphConverter(
            gson = gson,
            json = KotlinxGraphQLJson(),
            registry = get(),
        )
    }
    single {
        GraphConverter.create(
            json = KotlinxGraphQLJson(
                json = Json {
                    ignoreUnknownKeys = !BuildConfig.DEBUG
                },
            ),
            registry = get(),
        )
    }
    single {
        ClientInterceptor(
            agent = get(named("ua")),
        )
    }
}

private val retrofitModule = module {
    single<OkHttpClient>(named("anilist")) {
        val builder = OkHttpClient.Builder()
            .readTimeout(35, TimeUnit.SECONDS)
            .connectTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor { Timber.v(it) }
                    .setLevel(HttpLoggingInterceptor.Level.BODY),
            )
        }
        builder.addInterceptor(get<AuthInterceptor>())
        builder.addInterceptor(get<ClientInterceptor>())
        builder.addInterceptor(get<ChuckerInterceptor>())
        builder.build()
    }

    single<Retrofit>(named("anilist"), createdAtStart = true) {
        WebTokenRequest.getToken(androidContext())
        Retrofit.Builder()
            .client(get<OkHttpClient>(named("anilist")))
            // .addConverterFactory(get<GraphConverter>())
            .addConverterFactory(get<AniGraphConverter>())
            .baseUrl(BuildConfig.API_LINK)
            .build()
    }

    single<OkHttpClient>(named("giphy")) {
        val ctx = androidContext()
        val builder = OkHttpClient.Builder()
            .readTimeout(35, TimeUnit.SECONDS)
            .connectTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor { Timber.v(it) }
                    .setLevel(HttpLoggingInterceptor.Level.HEADERS),
            )
        }
        builder.addInterceptor(CacheInterceptor(ctx, true))
        builder.addInterceptor(get<ClientInterceptor>())
        builder.addNetworkInterceptor(NetworkCacheInterceptor(ctx, true))
        builder.cache(cacheProvider(ctx))
        builder.build()
    }

    single<Retrofit>(named("giphy")) {
        Retrofit.Builder()
            .client(get<OkHttpClient>(named("giphy")))
            .addConverterFactory(GsonConverterFactory.create(get<Gson>(named("api"))))
            .baseUrl(BuildConfig.GIPHY_LINK)
            .build()
    }

    single<OkHttpClient>(named("crunchyroll")) {
        val ctx = androidContext()
        val builder = OkHttpClient.Builder()
            .readTimeout(35, TimeUnit.SECONDS)
            .connectTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor { Timber.v(it) }
                    .setLevel(HttpLoggingInterceptor.Level.HEADERS),
            )
        }
        builder.addInterceptor(CacheInterceptor(ctx, true))
        builder.addNetworkInterceptor(NetworkCacheInterceptor(ctx, true))
        builder.cache(cacheProvider(ctx))
        builder.build()
    }

    single<Retrofit>(named("crunchyrollFeed")) {
        @Suppress("DEPRECATION")
        Retrofit.Builder()
            .client(get<OkHttpClient>(named("crunchyroll")))
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
            .baseUrl(BuildConfig.FEEDS_LINK)
            .build()
    }

    single<Retrofit>(named("crunchyroll")) {
        @Suppress("DEPRECATION")
        Retrofit.Builder()
            .client(get<OkHttpClient>(named("crunchyroll")))
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
            .baseUrl(BuildConfig.CRUNCHY_LINK)
            .build()
    }

    single<OkHttpClient>(named("repository")) {
        val builder = OkHttpClient.Builder()
            .readTimeout(35, TimeUnit.SECONDS)
            .connectTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor { Timber.v(it) }
                    .setLevel(HttpLoggingInterceptor.Level.HEADERS),
            )
        }
        builder.build()
    }

    single<Retrofit>(named("repository")) {
        Retrofit.Builder()
            .client(get<OkHttpClient>(named("repository")))
            .addConverterFactory(GsonConverterFactory.create(get<Gson>(named("api"))))
            .baseUrl(BuildConfig.APP_REPO)
            .build()
    }

    single<Gson>(named("api")) { ServiceFactory.gson }
}

private val serviceModule = module {
    single<MediaService> { get<Retrofit>(named("anilist")).create(MediaService::class.java) }
    single<UserService> { get<Retrofit>(named("anilist")).create(UserService::class.java) }
    single<BrowseService> { get<Retrofit>(named("anilist")).create(BrowseService::class.java) }
    single<SearchService> { get<Retrofit>(named("anilist")).create(SearchService::class.java) }
    single<StaffService> { get<Retrofit>(named("anilist")).create(StaffService::class.java) }
    single<CharacterService> { get<Retrofit>(named("anilist")).create(CharacterService::class.java) }
    single<FeedService> { get<Retrofit>(named("anilist")).create(FeedService::class.java) }
    single<StudioService> { get<Retrofit>(named("anilist")).create(StudioService::class.java) }
    single<GiphyService> { get<Retrofit>(named("giphy")).create(GiphyService::class.java) }
    single<BaseService> { get<Retrofit>(named("anilist")).create(BaseService::class.java) }
    single<RepositoryService> { get<Retrofit>(named("repository")).create(RepositoryService::class.java) }
    single<EpisodeService>(named("crunchyrollFeed")) { get<Retrofit>(named("crunchyrollFeed")).create(EpisodeService::class.java) }
    single<EpisodeService>(named("crunchyroll")) { get<Retrofit>(named("crunchyroll")).create(EpisodeService::class.java) }
}

private val repositoryModule = module {
    single { MediaRepository(mediaService = get()) }
    single { UserRepository(userService = get(), boxQuery = get()) }
    single { BrowseRepository(browseService = get()) }
    single { CharacterRepository(characterService = get()) }
    single { StaffRepository(staffService = get()) }
    single { StudioRepository(studioService = get()) }
    single { SearchRepository(searchService = get()) }
    single { FeedRepository(feedService = get()) }
    single { BaseRepository(baseService = get(), boxQuery = get()) }
    single { CrunchyrollRepository(feedService = get(named("crunchyrollFeed")), crunchyrollService = get(named("crunchyroll"))) }
    single { WidgetMutationCoordinator(baseRepository = get(), browseRepository = get(), userRepository = get(), feedRepository = get(), coroutineScope = get(), ioDispatcher = get(IoDispatcherQualifier), mainDispatcher = get(MainDispatcherQualifier), databaseHelper = get()) }
}

private val mediaFeatureModule = module {
    viewModel { AiringListViewModel(browseRepository = get()) }
    viewModel { BrowseReviewViewModel(browseRepository = get()) }
    viewModel { MediaBrowseViewModel(baseRepository = get(), browseRepository = get()) }
    viewModel { MediaLatestViewModel(browseRepository = get()) }
    viewModel { MediaListViewModel(browseRepository = get(), userRepository = get(), settings = get()) }
    viewModel { ReviewViewModel(browseRepository = get()) }
    viewModel { SuggestionListViewModel(userRepository = get(), browseRepository = get()) }
    viewModel { MediaCharacterViewModel(mediaRepository = get()) }
    viewModel { MediaFeedViewModel(mediaRepository = get(), baseRepository = get()) }
    viewModel { MediaOverviewViewModel(repository = get(), settings = get<Settings>()) }
    viewModel { MediaRecommendationsViewModel(mediaRepository = get()) }
    viewModel { MediaRelationViewModel(mediaRepository = get()) }
    viewModel { MediaStaffViewModel(mediaRepository = get()) }
    viewModel { MediaStatsViewModel(mediaRepository = get()) }
    viewModel { MediaViewModel(mediaRepository = get(), baseRepository = get()) }
    viewModel { MediaSearchViewModel(searchRepository = get()) }
    viewModel { MediaFavouritesViewModel(userRepository = get()) }
}

private val userFeatureModule = module {
    viewModel { MainViewModel(userRepository = get()) }
    viewModel { UserOverviewViewModel(userRepository = get()) }
    viewModel { UserFeedViewModel(feedRepository = get(), baseRepository = get()) }
    viewModel { UserListViewModel(userRepository = get()) }
    viewModel { UserSearchViewModel(searchRepository = get()) }
    viewModel { NotificationViewModel(userRepository = get()) }
    viewModel { ProfileViewModel(userRepository = get()) }
    viewModel { FeedListViewModel(feedRepository = get(), baseRepository = get()) }
    viewModel { MessageFeedViewModel(feedRepository = get(), baseRepository = get()) }
    viewModel { LoginUserViewModel(userRepository = get()) }
}

private val characterFeatureModule = module {
    viewModel { CharacterViewModel(characterRepository = get(), baseRepository = get()) }
    viewModel { CharacterOverviewViewModel(characterRepository = get()) }
    viewModel { CharacterActorsViewModel(characterRepository = get()) }
    viewModel { CharacterSearchViewModel(searchRepository = get()) }
    viewModel { CharacterFavouritesViewModel(userRepository = get()) }
}

private val staffFeatureModule = module {
    viewModel { StaffViewModel(staffRepository = get(), baseRepository = get()) }
    viewModel { StaffOverviewViewModel(staffRepository = get()) }
    viewModel { StaffSearchViewModel(searchRepository = get()) }
    viewModel { StaffFavouritesViewModel(userRepository = get()) }
    viewModel { MediaStaffRoleViewModel(staffRepository = get()) }
    viewModel { MediaAnimeRoleViewModel(staffRepository = get()) }
    viewModel { MediaFormatViewModel(characterRepository = get(), staffRepository = get()) }
}

private val studioFeatureModule = module {
    viewModel { StudioViewModel(studioRepository = get(), baseRepository = get()) }
    viewModel { StudioMediaViewModel(studioRepository = get()) }
    viewModel { StudioSearchViewModel(searchRepository = get()) }
    viewModel { StudioFavouritesViewModel(userRepository = get()) }
}

private val utilityFeatureModule = module {
    viewModel { GiphyViewModel(giphyService = get()) }
    viewModel { LoginAuthViewModel() }
    viewModel {
        LoggingViewModel(
            logFileProvider = { get<File>(named("logFile")) },
            metadataProvider = get<MetadataProvider>(),
        )
    }
    factory<File>(named("logFile")) {
        androidContext().logFile()
    }
    factory<MetadataProvider> {
        {
            buildString {
                appendLine("# v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})")
                appendLine("# ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine("# Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                appendLine()
            }
        }
    }
}

val appModules = module {
    includes(
        coroutineModule,
        coreModule,
        widgetModule,
        workerModule,
        presenterModule,
        networkModule,
        retrofitModule,
        serviceModule,
        repositoryModule,
        mediaFeatureModule,
        userFeatureModule,
        characterFeatureModule,
        staffFeatureModule,
        studioFeatureModule,
        utilityFeatureModule,
    )
}
