package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.graphql.generated.CharacterSearch
import com.mxt.anitrend.graphql.generated.CharacterSearchData
import com.mxt.anitrend.graphql.generated.CharacterSort
import com.mxt.anitrend.graphql.generated.MediaSearch
import com.mxt.anitrend.graphql.generated.MediaSearchData
import com.mxt.anitrend.graphql.generated.MediaSort
import com.mxt.anitrend.graphql.generated.MediaType
import com.mxt.anitrend.graphql.generated.StaffSearch
import com.mxt.anitrend.graphql.generated.StaffSearchData
import com.mxt.anitrend.graphql.generated.StaffSort
import com.mxt.anitrend.graphql.generated.StudioSearch
import com.mxt.anitrend.graphql.generated.StudioSearchData
import com.mxt.anitrend.graphql.generated.StudioSort
import com.mxt.anitrend.graphql.generated.UserSearch
import com.mxt.anitrend.graphql.generated.UserSearchData
import com.mxt.anitrend.graphql.generated.UserSort
import com.mxt.anitrend.model.api.retro.anilist.SearchService
import com.mxt.anitrend.model.entity.anilist.MediaList
import com.mxt.anitrend.model.entity.anilist.meta.AiringSchedule
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.model.entity.anilist.meta.ImageBase
import com.mxt.anitrend.model.entity.anilist.meta.MediaTitle
import com.mxt.anitrend.model.entity.anilist.meta.TitleBase
import com.mxt.anitrend.model.entity.base.CharacterBase
import com.mxt.anitrend.model.entity.base.MediaBase
import com.mxt.anitrend.model.entity.base.StaffBase
import com.mxt.anitrend.model.entity.base.StudioBase
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.container.attribute.PageInfo
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(
    private val searchService: SearchService,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    suspend fun searchMedia(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        type: MediaType? = null,
        format: com.mxt.anitrend.graphql.generated.MediaFormat? = null,
        startDate: String? = null,
        endDate: String? = null,
        season: com.mxt.anitrend.graphql.generated.MediaSeason? = null,
        genres: List<String>? = null,
        genresExclude: List<String>? = null,
        isAdult: Boolean? = null,
        sort: List<MediaSort>? = null,
    ): Result<PageContainer<MediaBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = MediaSearch.request(
                id = id, page = page, perPage = perPage,
                search = search, type = type, format = format,
                startDate = startDate, endDate = endDate,
                season = season, genres = genres,
                genresExclude = genresExclude, isAdult = isAdult,
                sort = sort,
            )
            val response = searchService.getMediaSearch(request)
            if (response.isSuccessful) {
                handleMediaSearch(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchStudio(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<StudioSort>? = null,
    ): Result<PageContainer<StudioBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getStudioSearch(request)
            if (response.isSuccessful) {
                handleStudioSearch(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchStaff(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<StaffSort>? = null,
    ): Result<PageContainer<StaffBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getStaffSearch(request)
            if (response.isSuccessful) {
                handleStaffSearch(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchCharacter(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<CharacterSort>? = null,
    ): Result<PageContainer<CharacterBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getCharacterSearch(request)
            if (response.isSuccessful) {
                handleCharacterSearch(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun searchUser(
        id: Int? = null,
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        sort: List<UserSort>? = null,
    ): Result<PageContainer<UserBase>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserSearch.request(id = id, page = page, perPage = perPage, search = search, sort = sort)
            val response = searchService.getUserSearch(request)
            if (response.isSuccessful) {
                handleUserSearch(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    // Response handlers: unwrap the generated GraphQL response envelope at the
    // repository boundary and map it into the legacy entity lane the public API
    // still exposes. GraphQL errors and null/absent data keep the exact semantics
    // of the legacy AniListContainer decoding: errors throw the first message,
    // absent data throws "Empty response body", and a null Page block throws
    // "Empty response body" instead of returning a successful empty page.

    private fun handleMediaSearch(body: GraphQLResponse<MediaSearchData>): PageContainer<MediaBase> {
        val data = handleGraphQLResponse(body)
        val page = data.page ?: throw IllegalStateException("Empty response body")
        return buildPageContainer(
            items = page.media.orEmpty().filterNotNull().map { it.toMediaBase() },
            pageInfo = page.pageInfo?.toPageInfo(),
        )
    }

    private fun handleStudioSearch(body: GraphQLResponse<StudioSearchData>): PageContainer<StudioBase> {
        val data = handleGraphQLResponse(body)
        val page = data.page ?: throw IllegalStateException("Empty response body")
        return buildPageContainer(
            items = page.studios.orEmpty().filterNotNull().map { it.toStudioBase() },
            pageInfo = page.pageInfo?.toPageInfo(),
        )
    }

    private fun handleStaffSearch(body: GraphQLResponse<StaffSearchData>): PageContainer<StaffBase> {
        val data = handleGraphQLResponse(body)
        val page = data.page ?: throw IllegalStateException("Empty response body")
        return buildPageContainer(
            items = page.staff.orEmpty().filterNotNull().map { it.toStaffBase() },
            pageInfo = page.pageInfo?.toPageInfo(),
        )
    }

    private fun handleCharacterSearch(body: GraphQLResponse<CharacterSearchData>): PageContainer<CharacterBase> {
        val data = handleGraphQLResponse(body)
        val page = data.page ?: throw IllegalStateException("Empty response body")
        return buildPageContainer(
            items = page.characters.orEmpty().filterNotNull().map { it.toCharacterBase() },
            pageInfo = page.pageInfo?.toPageInfo(),
        )
    }

    private fun handleUserSearch(body: GraphQLResponse<UserSearchData>): PageContainer<UserBase> {
        val data = handleGraphQLResponse(body)
        val page = data.page ?: throw IllegalStateException("Empty response body")
        return buildPageContainer(
            items = page.users.orEmpty().filterNotNull().map { it.toUserBase() },
            pageInfo = page.pageInfo?.toPageInfo(),
        )
    }

    private fun <T> buildPageContainer(
        items: List<T>,
        pageInfo: PageInfo?,
    ): PageContainer<T> = PageContainer<T>().apply {
        pageData = items
        if (pageInfo != null) {
            this.pageInfo = pageInfo
        }
    }

    // MediaSearch data mapping

    private fun MediaSearchData.PageMedia.toMediaBase(): MediaBase = MediaBase().apply {
        id = this@toMediaBase.id.toLong()
        title = this@toMediaBase.title?.toMediaTitle()
        coverImage = this@toMediaBase.coverImage?.toImageBase()
        bannerImage = this@toMediaBase.bannerImage
        type = this@toMediaBase.type?.name
        format = this@toMediaBase.format?.name
        season = this@toMediaBase.season?.name
        status = this@toMediaBase.status?.name
        siteUrl = this@toMediaBase.siteUrl
        meanScore = this@toMediaBase.meanScore ?: 0
        averageScore = this@toMediaBase.averageScore ?: 0
        startDate = this@toMediaBase.startDate?.toFuzzyDate()
        endDate = this@toMediaBase.endDate?.toFuzzyDate()
        episodes = this@toMediaBase.episodes ?: 0
        chapters = this@toMediaBase.chapters ?: 0
        volumes = this@toMediaBase.volumes ?: 0
        isAdult = this@toMediaBase.isAdult ?: false
        isFavourite = this@toMediaBase.isFavourite
        nextAiringEpisode = this@toMediaBase.nextAiringEpisode?.toAiringSchedule()
        mediaListEntry = this@toMediaBase.mediaListEntry?.toMediaList()
    }

    private fun MediaSearchData.PageMediaTitle.toMediaTitle(): MediaTitle = MediaTitle(
        romajiRaw = romaji,
        englishRaw = english,
        originalRaw = native,
        userPreferredRaw = userPreferred,
    )

    private fun MediaSearchData.PageMediaCoverImage.toImageBase(): ImageBase = ImageBase(
        extraLarge = extraLarge,
        large = large,
        medium = medium,
    )

    private fun MediaSearchData.PageMediaStartDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
        day = day ?: 0,
        month = month ?: 0,
        year = year ?: 0,
    )

    private fun MediaSearchData.PageMediaEndDate.toFuzzyDate(): FuzzyDate = FuzzyDate(
        day = day ?: 0,
        month = month ?: 0,
        year = year ?: 0,
    )

    private fun MediaSearchData.PageMediaNextAiringEpisode.toAiringSchedule(): AiringSchedule = AiringSchedule(
        airingAt = airingAt.toLong(),
        timeUntilAiring = timeUntilAiring.toLong(),
        episode = episode,
    )

    private fun MediaSearchData.PageMediaMediaListEntry.toMediaList(): MediaList = MediaList().apply {
        id = this@toMediaList.id.toLong()
        status = this@toMediaList.status?.name
    }

    private fun MediaSearchData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
        total = total ?: 0,
        perPage = perPage ?: 0,
        currentPage = currentPage ?: 0,
    ).apply {
        setHasNextPage(hasNextPage ?: false)
    }

    // StudioSearch data mapping

    private fun StudioSearchData.PageStudios.toStudioBase(): StudioBase = StudioBase().apply {
        id = this@toStudioBase.id.toLong()
        name = this@toStudioBase.name
        siteUrl = this@toStudioBase.siteUrl
        isFavourite = this@toStudioBase.isFavourite
    }

    private fun StudioSearchData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
        total = total ?: 0,
        perPage = perPage ?: 0,
        currentPage = currentPage ?: 0,
    ).apply {
        setHasNextPage(hasNextPage ?: false)
    }

    // StaffSearch data mapping

    private fun StaffSearchData.PageStaff.toStaffBase(): StaffBase = StaffBase().apply {
        id = this@toStaffBase.id.toLong()
        name = this@toStaffBase.name?.toTitleBase()
        image = this@toStaffBase.image?.toImageBase()
        isFavourite = this@toStaffBase.isFavourite
        language = this@toStaffBase.language?.name
        siteUrl = this@toStaffBase.siteUrl
    }

    private fun StaffSearchData.PageStaffName.toTitleBase(): TitleBase = TitleBase(
        first = first,
        last = last,
        original = native,
        alternative = alternative?.filterNotNull(),
    )

    private fun StaffSearchData.PageStaffImage.toImageBase(): ImageBase = ImageBase(
        extraLarge = null,
        large = large,
        medium = medium,
    )

    private fun StaffSearchData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
        total = total ?: 0,
        perPage = perPage ?: 0,
        currentPage = currentPage ?: 0,
    ).apply {
        setHasNextPage(hasNextPage ?: false)
    }

    // CharacterSearch data mapping

    private fun CharacterSearchData.PageCharacters.toCharacterBase(): CharacterBase = CharacterBase().apply {
        id = this@toCharacterBase.id.toLong()
        name = this@toCharacterBase.name?.toTitleBase()
        image = this@toCharacterBase.image?.toImageBase()
        isFavourite = this@toCharacterBase.isFavourite
        siteUrl = this@toCharacterBase.siteUrl
    }

    private fun CharacterSearchData.PageCharactersName.toTitleBase(): TitleBase = TitleBase(
        first = first,
        last = last,
        original = native,
        alternative = alternative?.filterNotNull(),
    )

    private fun CharacterSearchData.PageCharactersImage.toImageBase(): ImageBase = ImageBase(
        extraLarge = null,
        large = large,
        medium = medium,
    )

    private fun CharacterSearchData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
        total = total ?: 0,
        perPage = perPage ?: 0,
        currentPage = currentPage ?: 0,
    ).apply {
        setHasNextPage(hasNextPage ?: false)
    }

    // UserSearch data mapping

    private fun UserSearchData.PageUsers.toUserBase(): UserBase = UserBase(
        name = name,
        bannerImage = bannerImage,
        isFollowing = isFollowing ?: false,
    ).apply {
        id = this@toUserBase.id.toLong()
        avatar = this@toUserBase.avatar?.toImageBase()
    }

    private fun UserSearchData.PageUsersAvatar.toImageBase(): ImageBase = ImageBase(
        extraLarge = null,
        large = large,
        medium = medium,
    )

    private fun UserSearchData.PagePageInfo.toPageInfo(): PageInfo = PageInfo(
        total = total ?: 0,
        perPage = perPage ?: 0,
        currentPage = currentPage ?: 0,
    ).apply {
        setHasNextPage(hasNextPage ?: false)
    }
}
