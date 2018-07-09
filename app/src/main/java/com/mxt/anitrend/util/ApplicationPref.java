package com.mxt.anitrend.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.Log;

import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;

import java.util.Locale;

/**
 * Created by max on 2017/09/16.
 * Application preferences
 */

public class ApplicationPref {

    private Context context;

    /** Base Application Values */
    private final String _versionCode = "_versionCode";
    private final String _freshInstall = "_freshInstall";
    private final String _isAuthenticated = "_isAuthenticated";

    /** Application Base Options */
    public static final String _isLightTheme = "_isLightTheme";
    public static final String _updateChannel = "_updateChannel";

    /** Api Keys */
    private final String _sortOrder = "_sortOrder";
    private final String _mediaStatus = "_mediaStatus";
    private final String _mediaFormat = "_mediaFormat";
    private final String _mediaSource = "_mediaSource";
    private final String _airingSort = "_airingSort";
    private final String _characterSort = "_characterSort";
    private final String _mediaListSort = "_mediaListSort";
    private final String _mediaSort = "_mediaSort";
    private final String _mediaTrendSort = "_mediaTrendSort";
    private final String _reviewSort = "_reviewSort";
    private final String _staffSort = "_staffSort";

    private SharedPreferences sharedPreferences;

    public ApplicationPref(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public boolean isAuthenticated() {
        return sharedPreferences.getBoolean(_isAuthenticated, false);
    }

    public void setAuthenticated(boolean authenticated) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(_isAuthenticated, authenticated);
        editor.apply();
    }

    public void toggleTheme() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(_isLightTheme, getTheme() == R.style.AppThemeLight ? sharedPreferences.getBoolean("amoled_theme",false) ? R.style.AppThemeAMODark : R.style.AppThemeDark : R.style.AppThemeLight);
        editor.apply();
    }

    public @StyleRes int getTheme() {
        @StyleRes int style = sharedPreferences.getInt(_isLightTheme, R.style.AppThemeLight);
        return style;
    }

    // Returns the IDs of the startup page
    public @IdRes int getStartupPage() {
        String id = sharedPreferences.getString(context.getString(R.string.pref_key_startup_page), "4");
        switch (id){
            case "0":
                return R.id.nav_home_feed;
            case "1":
                return R.id.nav_anime;
            case "2":
                return R.id.nav_manga;
            case "3":
                return R.id.nav_trending;
            case "4":
                return R.id.nav_airing;
            case "5":
                return R.id.nav_myanime;
            case "6":
                return R.id.nav_mymanga;
            case "7":
                return R.id.nav_hub;
            case "8":
                return R.id.nav_reviews;
        }
        return R.id.nav_airing;
    }

    public boolean isFreshInstall() {
        return sharedPreferences.getBoolean(_freshInstall, true);
    }

    public void setFreshInstall() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(_freshInstall, false);
        editor.apply();
    }

    public String getUserLanguage() {
            return sharedPreferences.getString(context.getString(R.string.pref_key_selected_Language), Locale.getDefault().getLanguage());
    }

    //Returns amount of time in seconds
    public int getSyncTime() {
        return Integer.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_sync_frequency), "15")) * 60;
    }

    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_new_message_notifications), true);
    }

    public String getNotificationsSound() {
        return sharedPreferences.getString(context.getString(R.string.pref_key_ringtone), "DEFAULT_SOUND");
    }

    public Boolean isCrashReportsEnabled() {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports), false);
    }

    public Boolean isUsageAnalyticsEnabled() {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports), false);
    }

    public void saveSeasonYear(int year) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KeyUtil.arg_seasonYear, year);
        editor.apply();
    }

    public int getSeasonYear() {
        return sharedPreferences.getInt(KeyUtil.arg_seasonYear, DateUtil.getCurrentYear(0));
    }


    public boolean shouldShowTipFor(@KeyUtil.TapTargetType String tipType) {
        return sharedPreferences.getBoolean(tipType, true);
    }

    public void disableTipFor(@KeyUtil.TapTargetType String tipType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(tipType, false);
        editor.apply();
    }


    public @KeyUtil.SortOrderType String getSortOrder() {
        return sharedPreferences.getString(_sortOrder, KeyUtil.DESC);
    }

    public void saveSortOrder(@KeyUtil.SortOrderType String sortOrder) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_sortOrder, sortOrder);
        editor.apply();
    }


    public @Nullable @KeyUtil.MediaStatus String getMediaStatus() {
        return sharedPreferences.getString(_mediaStatus, null);
    }

    public void setMediaStatus(@KeyUtil.MediaStatus String mediaStatus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_mediaStatus, mediaStatus);
        editor.apply();
    }


    public @Nullable @KeyUtil.MediaFormat String getMediaFormat() {
        return sharedPreferences.getString(_mediaFormat, null);
    }

    public void setMediaFormat(@KeyUtil.MediaFormat String mediaFormat) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_mediaFormat, mediaFormat);
        editor.apply();
    }


    public @Nullable @KeyUtil.MediaSource String getMediaSource() {
        return sharedPreferences.getString(_mediaSource, null);
    }

    public void setMediaSource(@KeyUtil.MediaSource String mediaSource) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_mediaSource, mediaSource);
        editor.apply();
    }


    public @KeyUtil.AiringSort String getAiringSort() {
        return sharedPreferences.getString(_airingSort, KeyUtil.EPISODE);
    }

    public void setAiringSort(@KeyUtil.AiringSort String airingSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_airingSort, airingSort);
        editor.apply();
    }


    public @KeyUtil.CharacterSort String getCharacterSort() {
        return sharedPreferences.getString(_characterSort, KeyUtil.ROLE);
    }

    public void setCharacterSort(@KeyUtil.CharacterSort String characterSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_characterSort, characterSort);
        editor.apply();
    }


    public @KeyUtil.MediaListSort String getMediaListSort() {
        return sharedPreferences.getString(_mediaListSort, KeyUtil.PROGRESS);
    }

    public void setMediaListSort(@KeyUtil.MediaListSort String mediaListSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_mediaListSort, mediaListSort);
        editor.apply();
    }


    public @KeyUtil.MediaSort String getMediaSort() {
        return sharedPreferences.getString(_mediaSort, KeyUtil.POPULARITY);
    }

    public void setMediaSort(@KeyUtil.MediaSort String mediaSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_mediaSort, mediaSort);
        editor.apply();
    }

    public @KeyUtil.MediaTrendSort String getMediaTrendSort() {
        return sharedPreferences.getString(_mediaTrendSort, KeyUtil.TRENDING);
    }

    public void setMediaTrendSort(@KeyUtil.MediaTrendSort String mediaTrendSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_mediaTrendSort, mediaTrendSort);
        editor.apply();
    }


    public @KeyUtil.ReviewSort String getReviewSort() {
        return sharedPreferences.getString(_reviewSort, KeyUtil.ID);
    }

    public void setReviewSort(@KeyUtil.ReviewSort String reviewSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_reviewSort, reviewSort);
        editor.apply();
    }


    public @KeyUtil.StaffSort String getStaffSort() {
        return sharedPreferences.getString(_staffSort, KeyUtil.ROLE);
    }

    public void setStaffSort(@KeyUtil.StaffSort String staffSort) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_staffSort, staffSort);
        editor.apply();
    }

    public @KeyUtil.Channel String getUpdateChannel() {
        return sharedPreferences.getString(_updateChannel, KeyUtil.STABLE);
    }

    public void setUpdateChannel(@KeyUtil.Channel String channel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(_updateChannel, channel);
        editor.apply();
    }

    public boolean isUpdated() {
        return sharedPreferences.getInt(_versionCode, 1) < BuildConfig.VERSION_CODE;
    }

    public void setUpdated() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(_versionCode, BuildConfig.VERSION_CODE);
        editor.apply();
    }
}
