package com.mxt.anitrend.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.StyleRes;

import com.mxt.anitrend.R;

import java.util.Set;

/**
 * Created by max on 2017/09/16.
 * Application preferences
 */

public class ApplicationPref {

    private Context context;

    private static final int DEFAULT_APP_CODE = 1;
    private static final String DEFAULT_APP_VERSION = "0.1";

    /** Base Application Values */
    private final String KEY_FRESH_INSTALL = "KEY_FRESH_INSTALL";
    private final String KEY_AUTHENTICATED = "KEY_AUTHENTICATED";

    /** Application Base Options */
    private final String KEY_LIGHT_THEME = "KEY_LIGHT_THEME";

    /** AniList Preferences */
    private static final String KEY_SORT_TYPE = "KEY_SORT_TYPE";
    private static final String KEY_ORDER_TYPE = "KEY_ORDER_TYPE";
    private static final String KEY_GENRE_TYPE = "KEY_GENRE_TYPE";
    private static final String KEY_SHOW_TYPE = "KEY_SHOW_TYPE";
    private static final String KEY_MANGA_SHOW_TYPE = "KEY_MANGA_SHOW_TYPE";
    private static final String KEY_FILTER_YEAR = "KEY_FILTER_YEAR";
    private static final String KEY_ANIME_STATUS_TYPE = "KEY_ANIME_STATUS_TYPE";
    private static final String KEY_MANGA_STATUS_TYPE = "KEY_MANGA_STATUS_TYPE";
    private static final String KEY_DEFAULT_GENRE_EXCLUDE = "KEY_DEFAULT_GENRE_EXCLUDE";
    private static final String KEY_GENRE_INDICES = "KEY_GENRE_INDICES";

    private SharedPreferences sharedPreferences;

    public ApplicationPref(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static boolean isKeyFilter(String key) {
        return key.equals(KEY_SORT_TYPE) || key.equals(KEY_ORDER_TYPE) || key.equals(KEY_GENRE_TYPE) ||
                key.equals(KEY_SHOW_TYPE) || key.equals(KEY_MANGA_SHOW_TYPE) || key.equals(KEY_FILTER_YEAR) ||
                key.equals(KEY_ANIME_STATUS_TYPE) || key.equals(KEY_MANGA_STATUS_TYPE) || key.equals(KEY_DEFAULT_GENRE_EXCLUDE) ||
                key.equals(KEY_GENRE_INDICES);
    }

    public boolean isAuthenticated() {
        return sharedPreferences.getBoolean(KEY_AUTHENTICATED, false);
    }

    public void setAuthenticated(boolean authenticated) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_AUTHENTICATED, authenticated);
        editor.apply();
    }

    public void toggleTheme() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_LIGHT_THEME, getTheme() == R.style.AppThemeLight ? R.style.AppThemeDark : R.style.AppThemeLight);
        editor.apply();
    }

    public @StyleRes int getTheme() {
        @StyleRes int style = sharedPreferences.getInt(KEY_LIGHT_THEME, R.style.AppThemeLight);
        return style;
    }

    //Returns the id of the startup page
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
        return sharedPreferences.getBoolean(KEY_FRESH_INSTALL, true);
    }

    public void setFreshInstall() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FRESH_INSTALL, false);
        editor.apply();
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

    public void saveSort(String sorting){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SORT_TYPE, sorting);
        editor.apply();
    }

    public String getSort() {
        return sharedPreferences.getString(KEY_SORT_TYPE, KeyUtils.SeriesSortTypes[KeyUtils.POPULARITY]);
    }

    public String getSort_by() {
        String order_by = getOrder(), sort_by = getSort();
        if ((order_by != null && order_by.equals("desc")) && !sort_by.contains("desc"))
            return String.format("%s-%s", sort_by, order_by);
        return sort_by;

    }

    public void saveAnimeMediaType(String show_type){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SHOW_TYPE, show_type);
        editor.apply();
    }

    public String getAnimeMediaType() {
        return sharedPreferences.getString(KEY_SHOW_TYPE, null);
    }

    public void saveMangaMediaType(String show_type){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MANGA_SHOW_TYPE, show_type);
        editor.apply();
    }

    public String getMangaMediaType() {
        return sharedPreferences.getString(KEY_MANGA_SHOW_TYPE, null);
    }


    public void saveOrder(@KeyUtils.SortOrderType String order) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ORDER_TYPE, order);
        editor.apply();
    }

    public @KeyUtils.SortOrderType String getOrder() {
        return sharedPreferences.getString(KEY_ORDER_TYPE, KeyUtils.DESC);
    }

    public void saveYear(int year) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_FILTER_YEAR, year);
        editor.apply();
    }

    public int getYear(){
        return sharedPreferences.getInt(KEY_FILTER_YEAR, DateUtil.getYear());
    }

    public void saveAnimeStatus(@KeyUtils.AnimeStatusType int status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ANIME_STATUS_TYPE, KeyUtils.AnimeStatusTypes[status]);
        editor.apply();
    }

    public String getAnimeStatus() {
        return sharedPreferences.getString(KEY_ANIME_STATUS_TYPE, KeyUtils.AnimeStatusTypes[KeyUtils.ALL_ITEMS]);
    }

    public void saveMangaStatus(@KeyUtils.MangaStatusType int status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MANGA_STATUS_TYPE, KeyUtils.MangaStatusTypes[status]);
        editor.apply();
    }

    public String getMangaStatus() {
        return sharedPreferences.getString(KEY_MANGA_STATUS_TYPE, KeyUtils.MangaStatusTypes[KeyUtils.ALL_ITEMS]);
    }

    public void saveExclude(String exclude){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DEFAULT_GENRE_EXCLUDE, exclude);
        editor.apply();
    }

    public String getExcluded() {
        return sharedPreferences.getString(KEY_DEFAULT_GENRE_EXCLUDE, "Hentai");
    }

    public void saveGenres(String genres){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_GENRE_TYPE, genres);
        editor.apply();
    }

    public String getGenres() {
        return sharedPreferences.getString(KEY_GENRE_TYPE, null);
    }

    public void saveGenresIndices(Set<String> genres){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_GENRE_INDICES, genres);
        editor.apply();
    }

    public Set<String> getGenresIndices() {
        return sharedPreferences.getStringSet(KEY_GENRE_INDICES, null);
    }

    public void resetGenres() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_GENRE_TYPE);
        editor.remove(KEY_GENRE_INDICES);
        editor.apply();
    }

    public boolean shouldShowTipFor(@KeyUtils.TapTargetType String tipType) {
        return sharedPreferences.getBoolean(tipType, true);
    }

    public void disableTipFor(@KeyUtils.TapTargetType String tipType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(tipType, false);
        editor.apply();
    }
}
