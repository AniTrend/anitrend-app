package com.mxt.anitrend.util

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.mxt.anitrend.BuildConfig
import com.mxt.anitrend.R
import java.util.*

/**
 * Created by max on 2017/09/16.
 * Application preferences
 */

class Settings(private val context: Context) {

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
            sharedPreferences.edit {
                putBoolean(_isAuthenticated, authenticated)
                apply()
            }
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

    var isFreshInstall: Boolean = true
    get() = sharedPreferences.getBoolean(_freshInstall, true)
    set(value) {
        field = value
        sharedPreferences.edit {
            putBoolean(_freshInstall, field)
            apply()
        }
    }

    var userLanguage: String = Locale.getDefault().language
        get() {
            return sharedPreferences.getString(
                    context.getString(R.string.pref_key_selected_Language),
                    Locale.getDefault().language
            ) ?: Locale.getDefault().language
        }
        set(value) {
            field = value
            sharedPreferences.edit {
                putString(context.getString(R.string.pref_key_selected_Language), field)
                apply()
            }
        }

    //Returns amount of time in seconds
    var syncTime: Int = 15
        get() {
            return sharedPreferences.getString(
                    context.getString(R.string.pref_key_sync_frequency),
                    "15"
            )?.toInt() ?: 15
        }
        set(value) {
            field = value
            sharedPreferences.edit {
                putInt(context.getString(R.string.pref_key_sync_frequency), field)
                apply()
            }
        }

    var isNotificationEnabled: Boolean = true
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_new_message_notifications), true)
        set(value) {
            field = value
            sharedPreferences.edit {
                putBoolean(context.getString(R.string.pref_key_new_message_notifications), field)
                apply()
            }
        }

    var notificationsSound: String = "DEFAULT_SOUND"
        get() {
            return sharedPreferences.getString(
                    context.getString(R.string.pref_key_ringtone),
                    "DEFAULT_SOUND"
            ) ?: "DEFAULT_SOUND"
        }
        set(value) {
            field = value
            sharedPreferences.edit {
                putString(context.getString(R.string.pref_key_ringtone), field)
                apply()
            }
        }

    var isCrashReportsEnabled: Boolean = false
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports), false)
        set(value) {
            field = value
            sharedPreferences.edit {
                putBoolean(context.getString(R.string.pref_key_crash_reports), field)
                apply()
            }
        }

    var isUsageAnalyticsEnabled: Boolean = false
        get() = sharedPreferences.getBoolean(context.getString(R.string.pref_key_usage_analytics), false)
        set(value) {
            field = value
            sharedPreferences.edit {
                putBoolean(context.getString(R.string.pref_key_usage_analytics), field)
                apply()
            }
        }

    var seasonYear: Int = 0
        get() {
            return sharedPreferences.getInt(
                    KeyUtil.arg_seasonYear,
                    DateUtil.getCurrentYear(0)
            )
        }
        set(value) {
            field = value
            sharedPreferences.edit {
                putInt(KeyUtil.arg_seasonYear, field)
                apply()
            }
        }

    @set:KeyUtil.SortOrderType
    @get:KeyUtil.SortOrderType
    var sortOrder: String = KeyUtil.DESC
        get() {
            return sharedPreferences.getString(
                    _sortOrder,
                    KeyUtil.DESC
            ) ?: KeyUtil.DESC
        }
        set(value) {
            field = value
            sharedPreferences.edit {
                putString(_sortOrder, value)
                apply()
            }
        }

    @set:KeyUtil.MediaStatus
    @get:KeyUtil.MediaStatus
    var mediaStatus: String?
        get() = sharedPreferences.getString(_mediaStatus, null)
        set(mediaStatus) {
            sharedPreferences.edit {
                putString(_mediaStatus, mediaStatus)
                apply()
            }
        }

    @set:KeyUtil.MediaFormat
    @get:KeyUtil.MediaFormat
    var mediaFormat: String?
        get() = sharedPreferences.getString(_mediaFormat, null)
        set(mediaFormat) {
            sharedPreferences.edit {
                putString(_mediaFormat, mediaFormat)
                apply()
            }
        }

    @set:KeyUtil.AnimeFormat
    @get:KeyUtil.AnimeFormat
    var animeFormat: String?
        get() = sharedPreferences.getString(_animeFormat, null)
        set(animeFormat) {
            sharedPreferences.edit {
                putString(_animeFormat, animeFormat)
                apply()
            }
        }

    @set:KeyUtil.MangaFormat
    @get:KeyUtil.MangaFormat
    var mangaFormat: String?
        get() = sharedPreferences.getString(_mangaFormat, null)
        set(mangaFormat) {
            sharedPreferences.edit {
                putString(_mangaFormat, mangaFormat)
                apply()
            }
        }

    @set:KeyUtil.MediaSource
    @get:KeyUtil.MediaSource
    var mediaSource: String?
        get() = sharedPreferences.getString(_mediaSource, null)
        set(mediaSource) {
            sharedPreferences.edit {
                putString(_mediaSource, mediaSource)
                apply()
            }
        }

    @set:KeyUtil.AiringSort
    @get:KeyUtil.AiringSort
    var airingSort: String?
        get() = sharedPreferences.getString(_airingSort, KeyUtil.EPISODE)
        set(airingSort) {
            sharedPreferences.edit {
                putString(_airingSort, airingSort)
                apply()
            }
        }

    @set:KeyUtil.CharacterSort
    @get:KeyUtil.CharacterSort
    var characterSort: String?
        get() = sharedPreferences.getString(_characterSort, KeyUtil.ROLE)
        set(characterSort) {
            sharedPreferences.edit {
                putString(_characterSort, characterSort)
                apply()
            }
        }

    @set:KeyUtil.MediaListSort
    @get:KeyUtil.MediaListSort
    var mediaListSort: String?
        get() = sharedPreferences.getString(_mediaListSort, KeyUtil.PROGRESS)
        set(mediaListSort) {
            sharedPreferences.edit {
                putString(_mediaListSort, mediaListSort)
                apply()
            }
        }

    @set:KeyUtil.MediaSort
    @get:KeyUtil.MediaSort
    var mediaSort: String?
        get() = sharedPreferences.getString(_mediaSort, KeyUtil.POPULARITY)
        set(mediaSort) {
            sharedPreferences.edit {
                putString(_mediaSort, mediaSort)
                apply()
            }
        }
    @set:KeyUtil.MediaTrendSort
    @get:KeyUtil.MediaTrendSort
    var mediaTrendSort: String?
        get() = sharedPreferences.getString(_mediaTrendSort, KeyUtil.TRENDING)
        set(mediaTrendSort) {
            sharedPreferences.edit {
                putString(_mediaTrendSort, mediaTrendSort)
                apply()
            }
        }

    @set:KeyUtil.ReviewSort
    @get:KeyUtil.ReviewSort
    var reviewSort: String?
        get() = sharedPreferences.getString(_reviewSort, KeyUtil.ID)
        set(reviewSort) {
            sharedPreferences.edit {
                putString(_reviewSort, reviewSort)
                apply()
            }
        }

    @set:KeyUtil.StaffSort
    @get:KeyUtil.StaffSort
    var staffSort: String?
        get() = sharedPreferences.getString(_staffSort, KeyUtil.ROLE)
        set(staffSort) {
            sharedPreferences.edit {
                putString(_staffSort, staffSort)
                apply()
            }
        }

    @set:KeyUtil.Channel
    @get:KeyUtil.Channel
    var updateChannel: String?
        get() = sharedPreferences.getString(_updateChannel, KeyUtil.STABLE)
        set(channel) {
            sharedPreferences.edit {
                putString(_updateChannel, channel)
                apply()
            }
        }

    val isUpdated: Boolean
        get() = versionCode < BuildConfig.VERSION_CODE

    var versionCode: Int = 1
        get() = sharedPreferences.getInt(_versionCode, 1)
        set(value) {
            field = value
            sharedPreferences.edit {
                putInt(_versionCode, value)
                apply()
            }
        }

    var selectedGenres: Map<Int, String>?
        get() {
            val selected = sharedPreferences.getString(_genreFilter, null)
            return GenreTagUtil().convertToEntity(selected)
        }
        set(selectedIndices) {
            val selected = GenreTagUtil()
                    .convertToJson(selectedIndices)
            sharedPreferences.edit {
                putString(_genreFilter, selected)
                apply()
            }
        }

    var selectedTags: Map<Int, String>?
        get() {
            val selected = sharedPreferences.getString(_tagFilter, null)
            return GenreTagUtil().convertToEntity(selected)
        }
        set(selectedIndices) {
            val selected = GenreTagUtil()
                    .convertToJson(selectedIndices)
            sharedPreferences.edit {
                putString(_tagFilter, selected)
                apply()
            }
        }

    fun toggleTheme() {
        sharedPreferences.edit {
            putInt(_isLightTheme, if (theme == R.style.AppThemeLight) R.style.AppThemeDark else R.style.AppThemeLight)
            apply()
        }
    }

    fun saveSeasonYear(year: Int) {
        sharedPreferences.edit {
            putInt(KeyUtil.arg_seasonYear, year)
            apply()
        }
    }


    fun shouldShowTipFor(@KeyUtil.TapTargetType tipType: String): Boolean {
        return sharedPreferences.getBoolean(tipType, true)
    }

    fun disableTipFor(@KeyUtil.TapTargetType tipType: String) {
        sharedPreferences.edit {
            putBoolean(tipType, false)
            apply()
        }
    }

    fun saveSortOrder(@KeyUtil.SortOrderType sortOrder: String) {
        sharedPreferences.edit {
            putString(_sortOrder, sortOrder)
            apply()
        }
    }

    fun setUpdated() {
        sharedPreferences.edit {
            putInt(_versionCode, BuildConfig.VERSION_CODE)
            apply()
        }
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
