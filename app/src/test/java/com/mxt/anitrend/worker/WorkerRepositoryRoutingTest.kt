package com.mxt.anitrend.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.model.entity.anilist.Genre
import com.mxt.anitrend.model.entity.anilist.MediaTag
import com.mxt.anitrend.model.entity.anilist.Notification
import com.mxt.anitrend.model.entity.anilist.User
import com.mxt.anitrend.model.entity.anilist.WebToken
import com.mxt.anitrend.model.entity.base.AuthBase
import com.mxt.anitrend.model.entity.base.VersionBase
import com.mxt.anitrend.model.entity.container.body.PageContainer
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.repository.BaseRepository
import com.mxt.anitrend.repository.UserRepository
import com.mxt.anitrend.util.NotificationUtil
import com.mxt.anitrend.util.Settings
import io.objectbox.Box
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
class WorkerRepositoryRoutingTest {

    private val context = mock(Context::class.java)
    private val workerParameters = mock(WorkerParameters::class.java)
    private val boxQuery = TestBoxQuery()
    private val settings = mock(Settings::class.java)
    private val presenter = BasePresenter(
        context = context,
        boxQuery = boxQuery,
        settings = settings,
    )
    private val baseRepository = mock(BaseRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val notificationUtil = mock(NotificationUtil::class.java)

    @Test
    fun `GenreSyncWorker gets genres from repository and persists entities`() = runTest {
        doReturn(Result.success(listOf("Action", "Drama"))).`when`(baseRepository).getGenres()
        val worker = GenreSyncWorker(
            context = context,
            workerParams = workerParameters,
            presenter = presenter,
            baseRepository = baseRepository,
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(baseRepository).getGenres()
        assertEquals(listOf("Action", "Drama"), boxQuery.genreCollection.map { genre -> genre.genre })
    }

    @Test
    fun `GenreSyncWorker returns failure when repository fails`() = runTest {
        doReturn(Result.failure<List<String>>(RuntimeException("Genre failed"))).`when`(baseRepository).getGenres()
        val worker = GenreSyncWorker(
            context = context,
            workerParams = workerParameters,
            presenter = presenter,
            baseRepository = baseRepository,
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        verify(baseRepository).getGenres()
        assertEquals(emptyList<Genre>(), boxQuery.genreCollection)
    }

    @Test
    fun `TagSyncWorker gets tags from repository and persists them`() = runTest {
        val tags = listOf(MediaTag(name = "Shounen"), MediaTag(name = "Space"))
        doReturn(Result.success(tags)).`when`(baseRepository).getTags()
        val worker = TagSyncWorker(
            context = context,
            workerParams = workerParameters,
            presenter = presenter,
            baseRepository = baseRepository,
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(baseRepository).getTags()
        assertEquals(tags, boxQuery.mediaTags)
    }

    @Test
    fun `TagSyncWorker returns failure when repository fails`() = runTest {
        doReturn(Result.failure<List<MediaTag>>(RuntimeException("Tag failed"))).`when`(baseRepository).getTags()
        val worker = TagSyncWorker(
            context = context,
            workerParams = workerParameters,
            presenter = presenter,
            baseRepository = baseRepository,
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        verify(baseRepository).getTags()
        assertEquals(emptyList<MediaTag>(), boxQuery.mediaTags)
    }

    @Test
    fun `NotificationWorker gets current user and notifications from repository on success`() = runTest {
        val user = User().apply { unreadNotificationCount = 1 }
        val notifications = PageContainer<Notification>().apply {
            pageData = listOf(Notification().apply { id = 100 })
        }
        doReturn(true).`when`(settings).isAuthenticated
        doReturn(Result.success(user)).`when`(userRepository).getCurrentUser(asHtml = false)
        doReturn(Result.success(notifications))
            .`when`(userRepository)
            .getUserNotifications(resetNotificationCount = false)
        val worker = NotificationWorker(
            context = context,
            workerParams = workerParameters,
            presenter = presenter,
            notificationUtil = notificationUtil,
            userRepository = userRepository,
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(userRepository).getCurrentUser(asHtml = false)
        verify(userRepository).saveCurrentUser(user)
        verify(userRepository).getUserNotifications(resetNotificationCount = false)
        verify(notificationUtil).createNotification(user, notifications)
    }

    @Test
    fun `ClearNotificationWorker gets current user and notifications from repository on success`() = runTest {
        val user = User().apply { unreadNotificationCount = 1 }
        val notifications = PageContainer<Notification>().apply {
            pageData = listOf(Notification().apply { id = 200 })
        }
        doReturn(true).`when`(settings).isAuthenticated
        doReturn(Result.success(user)).`when`(userRepository).getCurrentUser(asHtml = false)
        doReturn(Result.success(notifications))
            .`when`(userRepository)
            .getUserNotifications(resetNotificationCount = true)
        val worker = ClearNotificationWorker(
            context = context,
            workerParams = workerParameters,
            presenter = presenter,
            userRepository = userRepository,
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(userRepository).getCurrentUser(asHtml = false)
        verify(userRepository).saveCurrentUser(user)
        verify(userRepository).getUserNotifications(resetNotificationCount = true)
        verify(userRepository).saveNotificationHistory(notifications)
    }

    private class TestBoxQuery : BoxQuery {
        override var currentUser: User? = null
        override var authCode: AuthBase? = null
        override var webToken: WebToken? = null
        override var remoteVersion: VersionBase? = null
        override var mediaTags: List<MediaTag> = emptyList()
        override var genreCollection: List<Genre> = emptyList()

        override fun <S> getBoxStore(classType: Class<S>): Box<S> =
            throw UnsupportedOperationException("Not used in worker routing tests")

        override fun invalidateBoxStores() = Unit
    }
}
