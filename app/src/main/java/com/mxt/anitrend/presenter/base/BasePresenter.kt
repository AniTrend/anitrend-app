package com.mxt.anitrend.presenter.base

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.annimon.stream.Stream
import com.mxt.anitrend.base.custom.async.WebTokenRequest
import com.mxt.anitrend.base.custom.presenter.CommonPresenter
import com.mxt.anitrend.model.entity.anilist.user.UserStatisticTypes
import com.mxt.anitrend.model.entity.base.UserBase
import com.mxt.anitrend.model.entity.crunchy.MediaContent
import com.mxt.anitrend.model.entity.crunchy.Thumbnail
import com.mxt.anitrend.service.TagGenreService
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.migration.MigrationUtil
import com.mxt.anitrend.util.migration.Migrations
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by max on 2017/09/16.
 * General presenter for most objects
 */

open class BasePresenter(context: Context?) : CommonPresenter(context) {

    private var favouriteGenres: List<String>? = null
    private var favouriteTags: List<String>? = null
    private var favouriteYears: List<String>? = null
    private var favouriteFormats: List<String>? = null

    fun checkIfMigrationIsNeeded(): Boolean {
        if (!settings.isFreshInstall) {
            val migrationUtil = MigrationUtil.Builder()
                    .addMigration(Migrations.MIGRATION_101_108)
                    .addMigration(Migrations.MIGRATION_109_134)
                    .addMigration(Migrations.MIGRATION_135_136)
                    .build()
            return migrationUtil.applyMigration()
        }
        return true
    }

    fun checkGenresAndTags(fragmentActivity: FragmentActivity) {
        val intent = Intent(fragmentActivity, TagGenreService::class.java)
        fragmentActivity.startService(intent)
    }

    fun getThumbnail(thumbnails: List<Thumbnail>): String? {
        return if (CompatUtil.isEmpty(thumbnails)) null else thumbnails[0].url
    }

    fun getDuration(mediaContent: MediaContent): String {
        if (mediaContent.duration != null) {
            val timeSpan = Integer.valueOf(mediaContent.duration).toLong()
            val minutes = TimeUnit.SECONDS.toMinutes(timeSpan)
            val seconds = timeSpan - TimeUnit.MINUTES.toSeconds(minutes)
            return String.format(Locale.getDefault(), if (seconds < 10) "%d:0%d" else "%d:%d", minutes, seconds)
        }
        return "00:00"
    }

    fun getTopFavouriteGenres(limit: Int): List<String>? {
        if (CompatUtil.isEmpty(favouriteGenres)) {
            val userStats: UserStatisticTypes? = database.currentUser?.statistics
            if (database.currentUser != null && userStats != null) {
                if (!userStats.anime.genres.isNullOrEmpty()) {
                    favouriteGenres = userStats.anime.genres
                            .sortedByDescending {
                                it.count
                            }.filter {
                                it.genre != null
                            }.map {
                                it.genre!!
                            }.take(limit)
                }
            }
        }
        return favouriteGenres
    }

    fun getTopFavouriteTags(limit: Int): List<String>? {
        if (CompatUtil.isEmpty(favouriteTags)) {
            val userStats: UserStatisticTypes? = database.currentUser?.statistics
            if (database.currentUser != null && userStats != null) {
                if (!userStats.anime.tags.isNullOrEmpty()) {
                    favouriteTags = Stream.of(userStats.anime.tags)
                            .sortBy { (_, _, count) -> -count }
                            .filter { (tag) -> tag != null }
                            .map { (tag) -> tag!!.name }
                            .limit(limit.toLong()).toList()
                    favouriteTags = userStats.anime.tags.sortedByDescending {
                        it.count
                    }.filter {
                        it.tag != null
                    }.map {
                        it.tag!!.name
                    }.take(limit)
                }
            }
        }
        return favouriteTags
    }

    fun getTopFavouriteYears(limit: Int): List<String>? {
        if (CompatUtil.isEmpty(favouriteYears)) {
            val userStats: UserStatisticTypes? = database.currentUser?.statistics
            if (database.currentUser != null && userStats != null) {
                if (!userStats.anime.releaseYears.isNullOrEmpty()) {
                    favouriteYears = userStats.anime.releaseYears
                            .sortedByDescending {
                                it.count
                            }.filter {
                                it.releaseYear != null
                            }.map {
                                it.releaseYear?.toString()!!
                            }.take(limit)
                }
            }
        }
        return favouriteTags
    }

    fun getTopFormats(limit: Int): List<String>? {
        if (CompatUtil.isEmpty(favouriteFormats)) {
            val userStats: UserStatisticTypes? = database.currentUser?.statistics
            if (database.currentUser != null && userStats != null) {
                if (!userStats.anime.formats.isNullOrEmpty()) {
                    favouriteFormats = userStats.anime.formats
                            .sortedByDescending {
                                it.count
                            }.filter {
                                it.format != null
                            }.map {
                                it.format!!
                            }.take(limit)
                }
            }
        }
        return favouriteFormats
    }

    fun isCurrentUser(userId: Long): Boolean {
        return settings.isAuthenticated && database.currentUser != null &&
                userId != 0L && database.currentUser?.id == userId
    }

    fun isCurrentUser(userName: String?): Boolean {
        return settings.isAuthenticated && database.currentUser != null &&
                userName != null && database.currentUser?.name == userName
    }

    fun isCurrentUser(userId: Long, userName: String?): Boolean {
        return userName?.let { isCurrentUser(it) } ?: isCurrentUser(userId)
    }

    fun isCurrentUser(userBase: UserBase?): Boolean {
        return userBase != null && isCurrentUser(userBase.id)
    }

    fun checkValidAuth() {
        if (settings.isAuthenticated) {
            if (database.currentUser == null) {
                Timber.tag(TAG).w("Last attempt to authenticate failed, refreshing session!")
                WebTokenRequest.invalidateInstance(context)
            }
        }
    }

    companion object {
        private val TAG = BasePresenter::class.java.simpleName
    }
}
