package com.mxt.anitrend.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.structure.FilterTypes;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Maxwell on 10/26/2016.
 * Class which will be used to filter general series data
 */
public class ApiPreferences {

    private final String KEY_SORT_TYPE = "sort_by";
    private final String KEY_ORDER_TYPE = "order_by";
    private final String KEY_GENRE_TYPE = "filter_genres";
    private final String KEY_SHOW_TYPE = "filter_type";
    private final String KEY_FILTER_YEAR = "filter_year";
    private final String KEY_STATUS_TYPE = "filter_status";
    private final String KEY_DEFAULT_GENRE_EXCLUDE = "genre_exclude";
    private final String KEY_GENRE_INDICES = "genre_indices";

    /*AniList Prefs*/
    private final String KEY_LIST_ORDER = "list_order";
    private final String KEY_TITLE_LANGUAGE = "title_language";
    private final String KEY_SCORE_TYPE = "score_type";
    private final String KEY_ADVANCED_RATING = "advanced_rating";
    private final String KEY_ADVANCED_RATING_NAMES = "advanced_rating_names";
    private final String KEY_CUSTOM_ANIME_LIST = "custom_list_anime";
    private final String KEY_CUSTOM_MANGA_LIST = "custom_list_manga";

    private SharedPreferences sharedPreferences;

    public ApiPreferences(Context appContext) {
        String PREF_FILE = "BaseApiConfig";
        sharedPreferences = appContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    /**
     * Sets the current users API prefs
     */
    public void saveUserApiPreferences(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_LIST_ORDER, user.getList_order());
        editor.putString(KEY_TITLE_LANGUAGE, user.getTitle_language());
        editor.putInt(KEY_SCORE_TYPE, user.getScore_type());
        editor.putBoolean(KEY_ADVANCED_RATING, user.getAdvanced_rating());
        if(user.getAdvanced_rating())
            editor.putStringSet(KEY_ADVANCED_RATING_NAMES, new TreeSet<>(user.getAdvanced_rating_names()));
        if(user.getCustom_list_anime().size() > 0)
            editor.putStringSet(KEY_CUSTOM_ANIME_LIST, new TreeSet<>(user.getCustom_list_anime()));
        if(user.getCustom_list_manga().size() > 0)
            editor.putStringSet(KEY_CUSTOM_MANGA_LIST, new TreeSet<>(user.getCustom_list_manga()));
        editor.apply();
    }

    /*
    * List oder configuration
    * 0 - Sort by score
    * 1 - Sort Alphabetical
    */
    public int getListOrder() {
        return sharedPreferences.getInt(KEY_LIST_ORDER, 0);
    }

    public String getTitleLanguage() {
        return sharedPreferences.getString(KEY_TITLE_LANGUAGE, "english");
    }

    public int getScoreType() {
        return sharedPreferences.getInt(KEY_SCORE_TYPE, 0);
    }

    public boolean hasAdvancedRating() {
        return sharedPreferences.getBoolean(KEY_ADVANCED_RATING, false);
    }

    public Set<String> getAdvancedRatingNames() {
        return sharedPreferences.getStringSet(KEY_ADVANCED_RATING_NAMES, null);
    }

    public Set<String> getCustomAnimeListNames() {
        return sharedPreferences.getStringSet(KEY_CUSTOM_ANIME_LIST, null);
    }

    public Set<String> getCustomMangaListNames() {
        return sharedPreferences.getStringSet(KEY_CUSTOM_MANGA_LIST, null);
    }

    public void saveSort(String sorting){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SORT_TYPE, sorting);
        editor.apply();
    }

    public String getSort() {
        return sharedPreferences.getString(KEY_SORT_TYPE, FilterTypes.SeriesSortTypes[FilterTypes.SeriesSortType.POPULARITY.ordinal()]);
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

    public void saveShowType(String show_type){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SHOW_TYPE, show_type);
        editor.apply();
    }

    public String getShowType() {
        return sharedPreferences.getString(KEY_SHOW_TYPE, null);
    }


    public void saveOrder(String order) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ORDER_TYPE, order);
        editor.apply();
    }

    public String getOrder() {
        return sharedPreferences.getString(KEY_ORDER_TYPE, FilterTypes.OrderTypes[FilterTypes.OrderType.DESC.ordinal()]);
    }

    public void saveYear(int year){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_FILTER_YEAR, year);
        editor.apply();
    }

    public int getYear(){
        return sharedPreferences.getInt(KEY_FILTER_YEAR, DateTimeConverter.getYear());
    }

    public void saveStatus(String status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_STATUS_TYPE, status);
        editor.apply();
    }

    public String getStatus() {
        return sharedPreferences.getString(KEY_STATUS_TYPE, FilterTypes.AnimeStatusTypes[FilterTypes.AnimeStatusType.ALL_ITEMS.ordinal()]);
    }

    public void saveExclude(String exclude){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DEFAULT_GENRE_EXCLUDE, exclude);
        editor.apply();
    }

    public String getExcluded() {
        return sharedPreferences.getString(KEY_DEFAULT_GENRE_EXCLUDE, "Hentai");
    }

}
