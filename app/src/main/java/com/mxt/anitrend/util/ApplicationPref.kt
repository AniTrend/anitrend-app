package com.mxt.anitrend.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.IdRes
import androidx.annotation.StyleRes

import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import com.mxt.anitrend.util.ApplicationPref.Companion._isLightTheme

import java.util.Locale

/**
 * Created by max on 2017/09/16.
 * Application preferences
 */

class ApplicationPref(private val context: Context) {

    /** Base Application Values  */
    private val _versionCode = "_versionCode"
    private val _freshInstall = "_freshInstall"
    private val _isAuthenticated = "_isAuthenticated"

    val sharedPreferences: SharedPreferences by lazy(LazyThreadSafetyMode.NONE) {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    var isAuthenticated: Boolean
        get() = sharedPreferences.getBoolean(_isAuthenticated, false)
        set(authenticated) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(_isAuthenticated, authenticated)
            editor.apply()
        }

    val theme: Int
        @StyleRes get() = sharedPreferences.getInt(_isLightTheme, R.style.AppThemeLight)

    val isBlackThemeEnabled: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_black_theme), false)

    // Returns the IDs of the startup page
    val startupPage: Int
        @IdRes get() {
            when (sharedPreferences.getString(context.getString(R.string.pref_key_startup_page), "4")) {
                "0" -> return R.id.nav_home_feed
                "1" -> return R.id.nav_anime
                "2" -> return R.id.nav_manga
                "3" -> return R.id.nav_trending
                "4" -> return R.id.nav_airing
                "5" -> return R.id.nav_myanime
                "6" -> return R.id.nav_mymanga
                "7" -> return R.id.nav_hub
                "8" -> return R.id.nav_reviews
            }
            return R.id.nav_airing
        }

    val isFreshInstall: Boolean
        get() = sharedPreferences.getBoolean(_freshInstall, true)

    val userLanguage: String?
        get() = sharedPreferences.getString(context.getString(R.string.pref_key_selected_Language), Locale.getDefault().language)

    //Returns amount of time in seconds
    val syncTime: Int
        get() = Integer.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_sync_frequency), "15")!!)

    val isNotificationEnabled: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_new_message_notifications), true)

    val notificationsSound: String?
        get() = sharedPreferences.getString(context.getString(R.string.pref_key_ringtone), "DEFAULT_SOUND")

    val isCrashReportsEnabled: Boolean?
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports), false)

    val isUsageAnalyticsEnabled: Boolean?
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports), false)

    val seasonYear: Int
        get() = sharedPreferences.getInt(KeyUtil.arg_seasonYear, DateUtil.getCurrentYear(0))


    val sortOrder: String?
        @KeyUtil.SortOrderType get() = sharedPreferences.getString(_sortOrder, KeyUtil.DESC)


    var mediaStatus: String?
        @KeyUtil.MediaStatus get() = sharedPreferences.getString(_mediaStatus, null)
        set(@KeyUtil.MediaStatus mediaStatus) {
            val editor = sharedPreferences.edit()
            editor.putString(_mediaStatus, mediaStatus)
            editor.apply()
        }


    var mediaFormat: String?
        @KeyUtil.MediaFormat get() = sharedPreferences.getString(_mediaFormat, null)
        set(@KeyUtil.MediaFormat mediaFormat) {
            val editor = sharedPreferences.edit()
            editor.putString(_mediaFormat, mediaFormat)
            editor.apply()
        }

    var animeFormat: String?
        @KeyUtil.AnimeFormat get() = sharedPreferences.getString(_animeFormat, null)
        set(@KeyUtil.AnimeFormat animeFormat) {
            val editor = sharedPreferences.edit()
            editor.putString(_animeFormat, animeFormat)
            editor.apply()
        }

    var mangaFormat: String?
        @KeyUtil.MangaFormat get() = sharedPreferences.getString(_mangaFormat, null)
        set(@KeyUtil.MangaFormat mangaFormat) {
            val editor = sharedPreferences.edit()
            editor.putString(_mangaFormat, mangaFormat)
            editor.apply()
        }


    var mediaSource: String?
        @KeyUtil.MediaSource get() = sharedPreferences.getString(_mediaSource, null)
        set(@KeyUtil.MediaSource mediaSource) {
            val editor = sharedPreferences.edit()
            editor.putString(_mediaSource, mediaSource)
            editor.apply()
        }


    var airingSort: String?
        @KeyUtil.AiringSort get() = sharedPreferences.getString(_airingSort, KeyUtil.EPISODE)
        set(@KeyUtil.AiringSort airingSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_airingSort, airingSort)
            editor.apply()
        }


    var characterSort: String?
        @KeyUtil.CharacterSort get() = sharedPreferences.getString(_characterSort, KeyUtil.ROLE)
        set(@KeyUtil.CharacterSort characterSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_characterSort, characterSort)
            editor.apply()
        }


    var mediaListSort: String?
        @KeyUtil.MediaListSort get() = sharedPreferences.getString(_mediaListSort, KeyUtil.PROGRESS)
        set(@KeyUtil.MediaListSort mediaListSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_mediaListSort, mediaListSort)
            editor.apply()
        }


    var mediaSort: String?
        @KeyUtil.MediaSort get() = sharedPreferences.getString(_mediaSort, KeyUtil.POPULARITY)
        set(@KeyUtil.MediaSort mediaSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_mediaSort, mediaSort)
            editor.apply()
        }

    var mediaTrendSort: String?
        @KeyUtil.MediaTrendSort get() = sharedPreferences.getString(_mediaTrendSort, KeyUtil.TRENDING)
        set(@KeyUtil.MediaTrendSort mediaTrendSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_mediaTrendSort, mediaTrendSort)
            editor.apply()
        }


    var reviewSort: String?
        @KeyUtil.ReviewSort get() = sharedPreferences.getString(_reviewSort, KeyUtil.ID)
        set(@KeyUtil.ReviewSort reviewSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_reviewSort, reviewSort)
            editor.apply()
        }


    var staffSort: String?
        @KeyUtil.StaffSort get() = sharedPreferences.getString(_staffSort, KeyUtil.ROLE)
        set(@KeyUtil.StaffSort staffSort) {
            val editor = sharedPreferences.edit()
            editor.putString(_staffSort, staffSort)
            editor.apply()
        }

    var updateChannel: String?
        @KeyUtil.Channel get() = sharedPreferences.getString(_updateChannel, KeyUtil.STABLE)
        set(@KeyUtil.Channel channel) {
            val editor = sharedPreferences.edit()
            editor.putString(_updateChannel, channel)
            editor.apply()
        }

    val isUpdated: Boolean
        get() = sharedPreferences.getInt(_versionCode, 1) < BuildConfig.VERSION_CODE

    var selectedGenres: Map<Int, String>?
        get() {
            val selected = sharedPreferences.getString(_genreFilter, null)
            return GenreTagUtil().convertToEntity(selected)
        }
        set(selectedIndices) {
            val selected = GenreTagUtil()
                    .convertToJson(selectedIndices)
            val editor = sharedPreferences.edit()
            editor.putString(_genreFilter, selected)
            editor.apply()
        }

    var selectedTags: Map<Int, String>?
        get() {
            val selected = sharedPreferences.getString(_tagFilter, null)
            return GenreTagUtil().convertToEntity(selected)
        }
        set(selectedIndices) {
            val selected = GenreTagUtil()
                    .convertToJson(selectedIndices)
            val editor = sharedPreferences.edit()
            editor.putString(_tagFilter, selected)
            editor.apply()
        }

    fun toggleTheme() {
        val editor = sharedPreferences.edit()
        editor.putInt(_isLightTheme, if (theme == R.style.AppThemeLight) R.style.AppThemeDark else R.style.AppThemeLight)
        editor.apply()
    }

    fun setFreshInstall() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(_freshInstall, false)
        editor.apply()
    }

    fun saveSeasonYear(year: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(KeyUtil.arg_seasonYear, year)
        editor.apply()
    }


    fun shouldShowTipFor(@KeyUtil.TapTargetType tipType: String): Boolean {
        return sharedPreferences.getBoolean(tipType, true)
    }

    fun disableTipFor(@KeyUtil.TapTargetType tipType: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(tipType, false)
        editor.apply()
    }

    fun saveSortOrder(@KeyUtil.SortOrderType sortOrder: String) {
        val editor = sharedPreferences.edit()
        editor.putString(_sortOrder, sortOrder)
        editor.apply()
    }

    fun setUpdated() {
        val editor = sharedPreferences.edit()
        editor.putInt(_versionCode, BuildConfig.VERSION_CODE)
        editor.apply()
    }

    companion object {

        /** Application Base Options  */
        const val _isLightTheme = "_isLightTheme"
        const val _updateChannel = "_updateChannel"

        /** Api Keys  */
        private const val _genreFilter = "_genreFilter"
        private const val _tagFilter = "_tagFilter"
        private const val _sortOrder = "_sortOrder"
        private const val _mediaStatus = "_mediaStatus"
        private const val _mediaFormat = "_mediaFormat"
        private const val _animeFormat = "_animeFormat"
        private const val _mangaFormat = "_mangaFormat"
        private const val _mediaSource = "_mediaSource"
        private const val _airingSort = "_airingSort"
        private const val _characterSort = "_characterSort"
        const val _mediaListSort = "_mediaListSort"
        private const val _mediaSort = "_mediaSort"
        private const val _mediaTrendSort = "_mediaTrendSort"
        private const val _reviewSort = "_reviewSort"
        private const val _staffSort = "_staffSort"
    }
}
