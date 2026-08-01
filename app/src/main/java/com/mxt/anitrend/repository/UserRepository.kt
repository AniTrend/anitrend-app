package com.mxt.anitrend.repository

import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.graphql.generated.AnimeFavourites
import com.mxt.anitrend.graphql.generated.CharacterFavourites
import com.mxt.anitrend.graphql.generated.CurrentUser
import com.mxt.anitrend.graphql.generated.MangaFavourites
import com.mxt.anitrend.graphql.generated.NotificationType
import com.mxt.anitrend.graphql.generated.StaffFavourites
import com.mxt.anitrend.graphql.generated.StudioFavourites
import com.mxt.anitrend.graphql.generated.ToggleFollow
import com.mxt.anitrend.graphql.generated.UserBase
import com.mxt.anitrend.graphql.generated.UserFavouriteCount
import com.mxt.anitrend.graphql.generated.UserFollowers
import com.mxt.anitrend.graphql.generated.UserFollowing
import com.mxt.anitrend.graphql.generated.UserNotifications
import com.mxt.anitrend.graphql.generated.UserOverview
import com.mxt.anitrend.graphql.generated.UserStats
import com.mxt.anitrend.model.api.retro.anilist.UserService
import com.mxt.anitrend.model.entity.anilist.Favourite
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.base.NotificationHistory
import com.mxt.anitrend.model.entity.container.body.ConnectionContainer
import com.mxt.anitrend.model.entity.container.body.PageContainer
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
    fun saveNotificationHistory(notifications: PageContainer<Notification>) {
        val notificationHistories = notifications.pageData
            .map { notification -> NotificationHistory(notification.id) }

        boxQuery
            .getBoxStore(NotificationHistory::class.java)
            .put(notificationHistories)
    }

    suspend fun getCurrentUser(asHtml: Boolean = false): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val request = CurrentUser.request(asHtml = asHtml)
            val response = userService.getCurrentUser(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }.onSuccess {
            boxQuery.currentUser = it
        }
    }

    suspend fun getUserBase(id: Long? = null, userName: String? = null): Result<UserEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = UserBase.request(id = id?.toInt(), userName = userName)
            val response = userService.getUserBase(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getUserOverview(id: Long? = null, userName: String? = null, asHtml: Boolean = false): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val request = UserOverview.request(id = id?.toInt(), userName = userName, asHtml = asHtml)
            val response = userService.getUserOverview(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getUserStats(id: Long? = null, userName: String? = null): Result<ConnectionContainer<UserStatisticTypes>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserStats.request(id = id?.toInt(), userName = userName)
            val response = userService.getUserStats(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
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
            val response = userService.getFollowers(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFollowing(
        id: Long,
        page: Int? = null,
        perPage: Int? = null,
        sort: List<com.mxt.anitrend.graphql.generated.UserSort>? = null,
    ): Result<PageContainer<UserEntity>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserFollowing.request(id = id.toInt(), page = page, perPage = perPage, sort = sort)
            val response = userService.getFollowing(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getFavouritesCount(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserFavouriteCount.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getFavouritesCount(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getAnimeFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = AnimeFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getAnimeFavourites(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getMangaFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = MangaFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getMangaFavourites(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getCharacterFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = CharacterFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getCharacterFavourites(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStaffFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = StaffFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getStaffFavourites(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getStudioFavourites(
        id: Long? = null,
        userName: String? = null,
        page: Int? = null,
        perPage: Int? = null,
    ): Result<ConnectionContainer<Favourite>> = withContext(ioDispatcher) {
        runCatching {
            val request = StudioFavourites.request(id = id?.toInt(), userName = userName, page = page, perPage = perPage)
            val response = userService.getStudioFavourites(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun getUserNotifications(
        page: Int? = null,
        perPage: Int? = null,
        type: NotificationType? = null,
        typeIn: List<NotificationType?>? = null,
        resetNotificationCount: Boolean? = false,
    ): Result<PageContainer<Notification>> = withContext(ioDispatcher) {
        runCatching {
            val request = UserNotifications.request(page = page, perPage = perPage, type = type, typeIn = typeIn, resetNotificationCount = resetNotificationCount)
            val response = userService.getUserNotifications(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }

    suspend fun toggleFollow(userId: Long): Result<UserEntity> = withContext(ioDispatcher) {
        runCatching {
            val request = ToggleFollow.request(userId = userId.toInt())
            val response = userService.toggleFollow(request).execute()
            if (response.isSuccessful) {
                handleGraphResponse(response.body() ?: throw IllegalStateException("Empty response body"))
            } else {
                throw RuntimeException(response.apiError())
            }
        }
    }
}
