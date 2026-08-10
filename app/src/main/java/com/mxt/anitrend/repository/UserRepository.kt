package com.mxt.anitrend.repository

import co.anitrend.retrofit.graphql.model.GraphQLResponse
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.data.mapper.applyUserSettingsTo
import com.mxt.anitrend.data.mapper.toNotificationPageResult
import com.mxt.anitrend.data.mapper.toUserSettingsRecord
import com.mxt.anitrend.domain.user.model.UserSettingsUpdate
import com.mxt.anitrend.data.mapper.toUserStatisticsRecord
import com.mxt.anitrend.domain.model.NotificationPageResult
import com.mxt.anitrend.domain.user.model.UserSettingsRecord
import com.mxt.anitrend.domain.user.model.UserStatisticsRecord
import com.mxt.anitrend.graphql.generated.AnimeFavourites
import com.mxt.anitrend.graphql.generated.AnimeFavouritesData
import com.mxt.anitrend.graphql.generated.CharacterFavourites
import com.mxt.anitrend.graphql.generated.CharacterFavouritesData
import com.mxt.anitrend.graphql.generated.CurrentUser
import com.mxt.anitrend.graphql.generated.CurrentUserData
import com.mxt.anitrend.graphql.generated.MangaFavourites
import com.mxt.anitrend.graphql.generated.MangaFavouritesData
import com.mxt.anitrend.graphql.generated.NotificationType
import com.mxt.anitrend.graphql.generated.ScoreFormat
import com.mxt.anitrend.graphql.generated.StaffFavourites
import com.mxt.anitrend.graphql.generated.StaffFavouritesData
import com.mxt.anitrend.graphql.generated.StudioFavourites
import com.mxt.anitrend.graphql.generated.StudioFavouritesData
import com.mxt.anitrend.graphql.generated.ToggleFollow
import com.mxt.anitrend.graphql.generated.ToggleFollowData
import com.mxt.anitrend.graphql.generated.UpdateUser
import com.mxt.anitrend.graphql.generated.UpdateUserData
import com.mxt.anitrend.graphql.generated.UserBase
import com.mxt.anitrend.graphql.generated.UserBaseData
import com.mxt.anitrend.graphql.generated.UserFavouriteCount
import com.mxt.anitrend.graphql.generated.UserFavouriteCountData
import com.mxt.anitrend.graphql.generated.UserFollowers
import com.mxt.anitrend.graphql.generated.UserFollowersData
import com.mxt.anitrend.graphql.generated.UserFollowing
import com.mxt.anitrend.graphql.generated.UserFollowingData
import com.mxt.anitrend.graphql.generated.UserNotifications
import com.mxt.anitrend.graphql.generated.UserNotificationsData
import com.mxt.anitrend.graphql.generated.UserOverview
import com.mxt.anitrend.graphql.generated.UserOverviewData
import com.mxt.anitrend.graphql.generated.UserStats
import com.mxt.anitrend.graphql.generated.UserStatsData
import com.mxt.anitrend.graphql.generated.UserTitleLanguage
import com.mxt.anitrend.model.api.retro.anilist.UserService
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.repository.mapper.toFavouriteConnection
import com.mxt.anitrend.repository.mapper.toUserBaseEntity
import com.mxt.anitrend.repository.mapper.toUserEntity
import com.mxt.anitrend.repository.mapper.toUserPage
import com.mxt.anitrend.util.graphql.apiError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mxt.anitrend.model.entity.base.UserBase as UserEntity

class UserRepository(
    private val userService: UserService,
    private val boxQuery: BoxQuery,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractRepository(ioDispatcher) {

    /** Cached current user from local DB. */
    val cachedCurrentUser: com.mxt.anitrend.model.entity.anilist.User?
        get() = boxQuery.currentUser

    /** Checks if the given user identity matches the cached current user. */
    fun isCurrentUser(userId: Long, userName: String?): Boolean {
        val current = boxQuery.currentUser ?: return false
        return userName?.let { current.name == it }
            ?: (userId != 0L && current.id == userId)
    }

    /** Persists the authenticated user in the existing current user cache. */
    fun saveCurrentUser(user: User) {
        boxQuery.currentUser = user
    }

    /** Persists notification ids as read notification history entries. */
    fun saveNotificationHistory(pageResult: NotificationPageResult) {
        val notificationHistories = pageResult.notifications
            .map { notification -> NotificationHistory(notification.id) }

        boxQuery
            .getBoxStore(NotificationHistory::class.java)
            .put(notificationHistories)
    }

    suspend fun getCurrentUser(asHtml: Boolean = false): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val request = CurrentUser.request(asHtml = asHtml)
            val response = userService.getCurrentUser(request)
            if (response.isSuccessful) {
                handleCurrentUser(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }.onSuccess {
            boxQuery.currentUser = it
        }
    }

    private fun handleCurrentUser(body: GraphQLResponse<CurrentUserData>): User {
        val data = handleGraphQLResponse(body)
        return data.viewer?.toUserEntity() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getUserBase(id: Long? = null, userName: String? = null): Result<UserEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = UserBase.request(id = id?.toInt(), userName = userName)
            val response = userService.getUserBase(request)
            if (response.isSuccessful) {
                handleUserBase(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleUserBase(body: GraphQLResponse<UserBaseData>): UserEntity {
        val data = handleGraphQLResponse(body)
        return data.user?.toUserBaseEntity() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getUserOverview(id: Long? = null, userName: String? = null, asHtml: Boolean = false): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val request = UserOverview.request(id = id?.toInt(), userName = userName, asHtml = asHtml)
            val response = userService.getUserOverview(request)
            if (response.isSuccessful) {
                handleUserOverview(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleUserOverview(body: GraphQLResponse<UserOverviewData>): User {
        val data = handleGraphQLResponse(body)
        return data.user?.toUserEntity() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getUserStats(id: Long? = null, userName: String? = null): Result<UserStatisticsRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = UserStats.request(id = id?.toInt(), userName = userName)
            val response = userService.getUserStats(request)
            if (response.isSuccessful) {
                handleUserStats(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFollowers(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.UserSort>? = null,
    ): Result<PageContainer<UserEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserFollowers.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = userService.getFollowers(request)
            if (response.isSuccessful) {
                handleFollowers(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleFollowers(body: GraphQLResponse<UserFollowersData>): PageContainer<UserEntity> {
        val data = handleGraphQLResponse(body)
        return data.page?.toUserPage() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getFollowing(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.UserSort>? = null,
    ): Result<PageContainer<UserEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserFollowing.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = userService.getFollowing(request)
            if (response.isSuccessful) {
                handleFollowing(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleFollowing(body: GraphQLResponse<UserFollowingData>): PageContainer<UserEntity> {
        val data = handleGraphQLResponse(body)
        return data.page?.toUserPage() ?: throw IllegalStateException("Empty response body")
    }

    suspend fun getFavouritesCount(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserFavouriteCount.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getFavouritesCount(request)
            if (response.isSuccessful) {
                handleFavouritesCount(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleFavouritesCount(body: GraphQLResponse<UserFavouriteCountData>): ConnectionContainer<Favourite> {
        val data = handleGraphQLResponse(body)
        return data.toFavouriteConnection()
    }

    suspend fun getAnimeFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = AnimeFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getAnimeFavourites(request)
            if (response.isSuccessful) {
                handleAnimeFavourites(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleAnimeFavourites(body: GraphQLResponse<AnimeFavouritesData>): ConnectionContainer<Favourite> {
        val data = handleGraphQLResponse(body)
        return data.toFavouriteConnection()
    }

    suspend fun getMangaFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = MangaFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getMangaFavourites(request)
            if (response.isSuccessful) {
                handleMangaFavourites(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleMangaFavourites(body: GraphQLResponse<MangaFavouritesData>): ConnectionContainer<Favourite> {
        val data = handleGraphQLResponse(body)
        return data.toFavouriteConnection()
    }

    suspend fun getCharacterFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getCharacterFavourites(request)
            if (response.isSuccessful) {
                handleCharacterFavourites(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleCharacterFavourites(body: GraphQLResponse<CharacterFavouritesData>): ConnectionContainer<Favourite> {
        val data = handleGraphQLResponse(body)
        return data.toFavouriteConnection()
    }

    suspend fun getStaffFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getStaffFavourites(request)
            if (response.isSuccessful) {
                handleStaffFavourites(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStaffFavourites(body: GraphQLResponse<StaffFavouritesData>): ConnectionContainer<Favourite> {
        val data = handleGraphQLResponse(body)
        return data.toFavouriteConnection()
    }

    suspend fun getStudioFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getStudioFavourites(request)
            if (response.isSuccessful) {
                handleStudioFavourites(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleStudioFavourites(body: GraphQLResponse<StudioFavouritesData>): ConnectionContainer<Favourite> {
        val data = handleGraphQLResponse(body)
        return data.toFavouriteConnection()
    }

    suspend fun getUserNotifications(
        page: Int? = null,
        perPage: Int? = null,
        type: NotificationType? = null,
        typeIn: List<NotificationType>? = null,
        resetNotificationCount: Boolean? = false,
    ): Result<NotificationPageResult> = withContext(ioDispatcher) {
        runCatching {
            val request = UserNotifications.request(page = page, perPage = perPage, type = type, typeIn = typeIn, resetNotificationCount = resetNotificationCount)
            val response = userService.getUserNotifications(request)
            if (response.isSuccessful) {
                handleUserNotifications(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleUserNotifications(body: GraphQLResponse<UserNotificationsData>): NotificationPageResult {
        val data = handleGraphQLResponse(body)
        return data.page?.toNotificationPageResult() ?: throw IllegalStateException("Empty response body")
    }

    private fun handleUserStats(body: GraphQLResponse<UserStatsData>): UserStatisticsRecord {
        val data = handleGraphQLResponse(body)
        return data.toUserStatisticsRecord()
    }

    suspend fun toggleFollow(userId: Long): Result<UserEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = ToggleFollow.request(userId = userId.toInt())
            val response = userService.toggleFollow(request)
            if (response.isSuccessful) {
                handleToggleFollow(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleToggleFollow(body: GraphQLResponse<ToggleFollowData>): UserEntity {
        val data = handleGraphQLResponse(body)
        return data.toggleFollow?.toUserBaseEntity() ?: throw IllegalStateException("Empty response body")
    }

    /**
     * Updates the bounded user settings slice through the `UpdateUser` mutation.
     *
     * Only the fields already read by `CurrentUser` are exposed: `about`,
     * `airingNotifications`, `displayAdultContent`, `profileColor`, `rowOrder`,
     * `scoreFormat` and `titleLanguage`. Absent parameters stay null on the wire
     * (the AniList backend treats them as "leave unchanged"), and the returned
     * [UserSettingsRecord] is mapped from the server response, keeping this
     * mutation server-authoritative with no optimistic updates.
     *
     * On success the response slice is also merged into the cached current user
     * through [saveCurrentUser] (see `applyUserSettingsTo`), so cached settings
     * stay in sync with the server without a full `CurrentUser` refetch.
     */
    suspend fun updateUser(update: UserSettingsUpdate = UserSettingsUpdate()): Result<UserSettingsRecord> = withContext(ioDispatcher) {
        runCatching {
            val request = UpdateUser.request(
                about = update.about,
                airingNotifications = update.airingNotifications,
                displayAdultContent = update.displayAdultContent,
                profileColor = update.profileColor,
                rowOrder = update.rowOrder,
                scoreFormat = update.scoreFormat?.let { ScoreFormat.valueOf(it) },
                titleLanguage = update.titleLanguage?.let { UserTitleLanguage.valueOf(it) },
            )
            val response = userService.updateUser(request)
            if (response.isSuccessful) {
                handleUpdateUser(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    private fun handleUpdateUser(body: GraphQLResponse<UpdateUserData>): UserSettingsRecord {
        val data = handleGraphQLResponse(body)
        val updatedUser = data.updateUser ?: throw IllegalStateException("Empty response body")
        applyServerUserSettingsToCachedUser(updatedUser)
        return updatedUser.toUserSettingsRecord()
    }

    private fun applyServerUserSettingsToCachedUser(updatedUser: UpdateUserData.UpdateUser) {
        val cachedUser = boxQuery.currentUser ?: return
        updatedUser.applyUserSettingsTo(cachedUser)
        saveCurrentUser(cachedUser)
    }
}
