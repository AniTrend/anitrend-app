package com.mxt.anitrend.koin

import android.app.NotificationManager
import android.content.Context
import android.webkit.WebSettings
import co.anitrend.retrofit.graphql.model.GraphQLDocumentRegistry
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
import com.mxt.anitrend.graphql.generated.GeneratedGraphQLRegistry
import com.mxt.anitrend.model.api.converter.AniGraphConverter
import com.mxt.anitrend.model.api.interceptor.AuthInterceptor
import com.mxt.anitrend.model.api.interceptor.CacheInterceptor
import com.mxt.anitrend.model.api.interceptor.ClientInterceptor
import com.mxt.anitrend.model.api.interceptor.NetworkCacheInterceptor
import com.mxt.anitrend.model.api.retro.WebFactory
import com.mxt.anitrend.model.api.retro.anilist.BaseModel
import com.mxt.anitrend.model.api.retro.anilist.BrowseModel
import com.mxt.anitrend.model.api.retro.anilist.CharacterModel
import com.mxt.anitrend.model.api.retro.anilist.FeedModel
import com.mxt.anitrend.model.api.retro.anilist.MediaModel
import com.mxt.anitrend.model.api.retro.anilist.SearchModel
import com.mxt.anitrend.model.api.retro.anilist.StaffModel
import com.mxt.anitrend.model.api.retro.anilist.StudioModel
import com.mxt.anitrend.model.api.retro.anilist.UserModel
import com.mxt.anitrend.model.api.retro.base.GiphyModel
import com.mxt.anitrend.model.api.retro.base.RepositoryModel
import com.mxt.anitrend.model.entity.MyObjectBox
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.presenter.widget.WidgetPresenter
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.BrowseRepository
import com.mxt.anitrend.repository.CharacterRepository
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

private val coreModule = module {
    single {
        MyObjectBox.builder()
            .androidContext(androidContext())
            .build()
    }

    single<BoxQuery> {
        DatabaseHelper()
    }

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
            userService = get(),
            userRepository = get(),
        )
    }
    worker { scope ->
        ClearNotificationWorker(
            context = androidContext(),
            workerParams = scope.get(),
        )
    }
    worker { scope ->
        GenreSyncWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            baseService = get(),
        )
    }
    worker { scope ->
        TagSyncWorker(
            context = androidContext(),
            workerParams = scope.get(),
            presenter = get(),
            baseService = get(),
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
        BasePresenter(androidContext())
    }
    factory {
        WidgetPresenter<Any>(androidContext())
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

    single<Gson>(named("api")) { WebFactory.gson }
}

private val serviceModule = module {
    single<MediaModel> { get<Retrofit>(named("anilist")).create(MediaModel::class.java) }
    single<UserModel> { get<Retrofit>(named("anilist")).create(UserModel::class.java) }
    single<BrowseModel> { get<Retrofit>(named("anilist")).create(BrowseModel::class.java) }
    single<SearchModel> { get<Retrofit>(named("anilist")).create(SearchModel::class.java) }
    single<StaffModel> { get<Retrofit>(named("anilist")).create(StaffModel::class.java) }
    single<CharacterModel> { get<Retrofit>(named("anilist")).create(CharacterModel::class.java) }
    single<FeedModel> { get<Retrofit>(named("anilist")).create(FeedModel::class.java) }
    single<StudioModel> { get<Retrofit>(named("anilist")).create(StudioModel::class.java) }
    single<GiphyModel> { get<Retrofit>(named("giphy")).create(GiphyModel::class.java) }
    single<BaseModel> { get<Retrofit>(named("anilist")).create(BaseModel::class.java) }
    single<RepositoryModel> { get<Retrofit>(named("repository")).create(RepositoryModel::class.java) }
}

private val repositoryModule = module {
    single { MediaRepository(mediaService = get()) }
    single { UserRepository(userService = get()) }
    single { BrowseRepository(browseService = get()) }
    single { CharacterRepository(characterService = get()) }
    single { StaffRepository(staffService = get()) }
    single { StudioRepository(studioService = get()) }
    single { SearchRepository(searchService = get()) }
    single { FeedRepository(feedService = get()) }
    single { BaseRepository(baseService = get()) }
    single { WidgetMutationCoordinator(baseRepository = get(), browseRepository = get(), userRepository = get(), feedRepository = get(), databaseHelper = get()) }
}

private val mediaFeatureModule = module {
    viewModel { AiringListViewModel(browseService = get()) }
    viewModel { BrowseReviewViewModel(browseService = get()) }
    viewModel { MediaBrowseViewModel(browseService = get()) }
    viewModel { MediaLatestViewModel(browseService = get()) }
    viewModel { MediaListViewModel(browseRepository = get(), settings = get()) }
    viewModel { ReviewViewModel(browseService = get()) }
    viewModel { SuggestionListViewModel(browseService = get()) }
    viewModel { MediaCharacterViewModel(mediaService = get()) }
    viewModel { MediaFeedViewModel(mediaService = get()) }
    viewModel { MediaOverviewViewModel(repository = get(), settings = get<Settings>()) }
    viewModel { MediaRecommendationsViewModel(mediaService = get()) }
    viewModel { MediaRelationViewModel(mediaService = get()) }
    viewModel { MediaStaffViewModel(mediaService = get()) }
    viewModel { MediaStatsViewModel(mediaService = get()) }
    viewModel { MediaViewModel(mediaService = get(), baseRepository = get()) }
    viewModel { MediaSearchViewModel(searchService = get()) }
    viewModel { MediaFavouritesViewModel(userService = get()) }
}

private val userFeatureModule = module {
    viewModel { MainViewModel(userService = get()) }
    viewModel { UserOverviewViewModel(userService = get()) }
    viewModel { UserFeedViewModel(feedRepository = get()) }
    viewModel { UserListViewModel(userService = get()) }
    viewModel { UserSearchViewModel(searchService = get()) }
    viewModel { NotificationViewModel(userService = get()) }
    viewModel { ProfileViewModel(userService = get(), userRepository = get()) }
    viewModel { FeedListViewModel(feedRepository = get()) }
    viewModel { MessageFeedViewModel(feedRepository = get()) }
    viewModel { LoginUserViewModel(userService = get()) }
}

private val characterFeatureModule = module {
    viewModel { CharacterViewModel(characterService = get(), baseRepository = get()) }
    viewModel { CharacterOverviewViewModel(characterService = get()) }
    viewModel { CharacterActorsViewModel(characterService = get()) }
    viewModel { CharacterSearchViewModel(searchService = get()) }
    viewModel { CharacterFavouritesViewModel(userService = get()) }
}

private val staffFeatureModule = module {
    viewModel { StaffViewModel(staffService = get(), baseRepository = get()) }
    viewModel { StaffOverviewViewModel(staffService = get()) }
    viewModel { StaffSearchViewModel(searchService = get()) }
    viewModel { StaffFavouritesViewModel(userService = get()) }
    viewModel { MediaStaffRoleViewModel(staffService = get()) }
    viewModel { MediaAnimeRoleViewModel(staffService = get()) }
    viewModel { MediaFormatViewModel(characterRepository = get(), staffRepository = get()) }
}

private val studioFeatureModule = module {
    viewModel { StudioViewModel(studioService = get(), baseRepository = get()) }
    viewModel { StudioMediaViewModel(studioService = get()) }
    viewModel { StudioSearchViewModel(searchService = get()) }
    viewModel { StudioFavouritesViewModel(userService = get()) }
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
        File(androidContext().filesDir, "timber.log")
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

val appModules = listOf(
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
