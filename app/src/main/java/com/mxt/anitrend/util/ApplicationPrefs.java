package com.mxt.anitrend.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.mxt.anitrend.api.model.User;
import com.mxt.anitrend.api.model.UserSmall;

/**
 * Created by Maxwell on 9/24/2016.
 * Global Preference Class
 */

public class ApplicationPrefs {

    private static final int DEFAULT_APP_CODE = 1;
    private static final String DEFAULT_APP_VERSION = "0.1";

    /*Application Tips*/
    private final String KEY_MAIN_TIP = "app_main_tip";
    private final String KEY_DETAIL_TIP = "app_detail_tip";
    private final String KEY_NOTIFICATION_TIP = "app_notification_tip";
    private final String KEY_MESSAGE_TIP = "app_message_tip";
    private final String KEY_COMPOSE_TIP = "app_compose_tip";
    private final String KEY_CHARACTER_TIP = "app_character_tip";
    private final String KEY_STAFF_TIP = "app_staff_tip";
    private final String KEY_STATUS_POST_TIP = "app_status_post_tip";
    private final String KEY_USER_PROFILE_TIP = "app_user_profile_tip";
    private final String KEY_REVIEW_TYPE_TIP = "app_review_type_tip";

    /*Base Application Values*/
    private final String KEY_VERSION_CODE = "app_code";
    private final String KEY_VERSION = "app_version";
    private final String KEY_REPO_VERSION_CODE = "app_repo_code";
    private final String KEY_REPO_VERSION = "app_repo_version";
    private final String KEY_AUTHENTICATED = "user_authenticated";
    private final String KEY_REFRESH = "app_key_reference";
    private final String KEY_GENRES_SAVED = "app_genres_saved";

    /*User related temp store*/
    private final String KEY_USER_ID = "app_user_id";
    private final String KEY_USER_NAME = "app_user_name";
    private final String KEY_USER_BANNER = "app_user_banner";
    private final String KEY_USER_MD_IMG = "app_user_profile_md";
    private final String KEY_USER_HG_IMG = "app_user_profile_hg";

    /*Application Base Options*/
    private final String KEY_LIGHT_THEME = "app_light_theme";
    private final String KEY_NEW_STYLE = "app_new_style";
    private final String KEY_REVIEW_TYPES = "app_review_type";

    private SharedPreferences sharedPreferences;

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public enum SmallUserKeys {
        USERID ,USERNAME ,IMG_MD, IMG_LG, IMG_BANNER
    }

    public boolean hasUserInfo() {
        return sharedPreferences.contains((KEY_USER_NAME));
    }

    public void setGenresSaved() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_GENRES_SAVED, true);
        editor.apply();
    }

    public boolean isGenresSaved() {
        return sharedPreferences.contains(KEY_GENRES_SAVED);
    }

    public boolean getNotificationTip() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_TIP, true);
    }

    public void setNotificationTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NOTIFICATION_TIP, false);
        editor.apply();
    }

    public boolean getStatusPostTip() {
        return sharedPreferences.getBoolean(KEY_STATUS_POST_TIP, true);
    }

    public void setStatusPost() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_STATUS_POST_TIP, false);
        editor.apply();
    }

    public boolean getUserProfileTip() {
        return sharedPreferences.getBoolean(KEY_USER_PROFILE_TIP, true);
    }

    public void setUserProfileTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_USER_PROFILE_TIP, false);
        editor.apply();
    }


    public boolean getMessageTip() {
        return sharedPreferences.getBoolean(KEY_MESSAGE_TIP, true);
    }

    public void setMessageTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_MESSAGE_TIP, false);
        editor.apply();
    }

    public boolean getComposeTip() {
        return sharedPreferences.getBoolean(KEY_COMPOSE_TIP, true);
    }

    public void setComposeTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_COMPOSE_TIP, false);
        editor.apply();
    }

    public boolean getCharacterTip() {
        return sharedPreferences.getBoolean(KEY_CHARACTER_TIP, true);
    }

    public void setCharacterTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_CHARACTER_TIP, false);
        editor.apply();
    }

    public boolean getStaffTip() {
        return sharedPreferences.getBoolean(KEY_STAFF_TIP, true);
    }

    public void setStaffTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_STAFF_TIP, false);
        editor.apply();
    }

    public void setNewStyle(boolean newStyle) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NEW_STYLE, newStyle);
        editor.apply();
    }

    public boolean isNewStyle() {
        return sharedPreferences.getBoolean(KEY_NEW_STYLE, true);
    }

    public ApplicationPrefs(Context appContext) {
        String PREF_FILE = "AppSettings";
        sharedPreferences = appContext.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    public boolean checkState() {
        return sharedPreferences.contains(KEY_VERSION_CODE) || sharedPreferences.contains(KEY_VERSION);
    }

    /**
     * Preference has never been created, call this method to edit or create a preference file
     * @param versionControl the current application version and code
     * */
    public void saveOrUpdateVersionNumber(AppVersionTracking versionControl){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_VERSION, versionControl.getVersion());
        editor.putInt(KEY_VERSION_CODE, versionControl.getCode());
        editor.apply();
    }

    public AppVersionTracking getSavedVersions() {
        return new AppVersionTracking(sharedPreferences.getInt(KEY_VERSION_CODE, DEFAULT_APP_CODE), sharedPreferences.getString(KEY_VERSION, DEFAULT_APP_VERSION));
    }

    public void saveRepoVersion(AppVersionTracking versionControl){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REPO_VERSION, versionControl.getVersion());
        editor.putInt(KEY_REPO_VERSION_CODE, versionControl.getCode());
        editor.apply();
    }

    public AppVersionTracking getRepoVersions() {
        return new AppVersionTracking(sharedPreferences.getInt(KEY_REPO_VERSION_CODE, DEFAULT_APP_CODE), sharedPreferences.getString(KEY_REPO_VERSION, DEFAULT_APP_VERSION));
    }

    public void setMainTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_MAIN_TIP, false);
        editor.apply();
    }

    public boolean getMainTip() {
        return sharedPreferences.getBoolean(KEY_MAIN_TIP, true);
    }

    public void setDetailTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DETAIL_TIP, false);
        editor.apply();
    }

    public boolean getDetailTip() {
        return sharedPreferences.getBoolean(KEY_DETAIL_TIP, true);
    }

    public void setUserAuthenticated(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_AUTHENTICATED, true);
        editor.apply();
    }

    public void setUserDeactivated(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_AUTHENTICATED, false);
        editor.putString(KEY_USER_ID, null);
        editor.putString(KEY_USER_NAME, null);
        editor.putString(KEY_USER_MD_IMG, null);
        editor.putString(KEY_USER_HG_IMG, null);
        editor.putString(KEY_USER_BANNER, null);
        editor.apply();
    }

    public boolean isAuthenticated(){
        return sharedPreferences.getBoolean(KEY_AUTHENTICATED, false);
    }

    public void setRefresh(String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REFRESH, refreshToken);
        editor.apply();
    }

    public String getRefresh(){
        return sharedPreferences.getString(KEY_REFRESH, null);
    }

    public void saveMiniUserInfo(User mCurrentUser) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, String.valueOf(mCurrentUser.getId()));
        editor.putString(KEY_USER_NAME,mCurrentUser.getDisplay_name());
        editor.putString(KEY_USER_MD_IMG,mCurrentUser.getImage_url_med());
        editor.putString(KEY_USER_HG_IMG,mCurrentUser.getImage_url_lge());
        editor.putString(KEY_USER_BANNER,mCurrentUser.getImage_url_banner());
        editor.apply();
    }

    /**
     * Be sure to get access to array indices using ApplicationPrefs.SmallUserKeys enum
     */
    public String[] getMiniUserInfo() {
        return new String[] {
            sharedPreferences.getString(KEY_USER_ID, null),
            sharedPreferences.getString(KEY_USER_NAME, null),
            sharedPreferences.getString(KEY_USER_MD_IMG, null),
            sharedPreferences.getString(KEY_USER_HG_IMG, null),
            sharedPreferences.getString(KEY_USER_BANNER, null)
        };
    }

    /**
     * Retrieves current users small object similar to what you'd get from the api
     */
    public UserSmall getMiniUser() {
        try {
            String[] values = getMiniUserInfo();
            return new UserSmall(
                    Integer.valueOf(values[0]),
                    values[1],
                    values[2],
                    values[3]
            );
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isLightTheme() {
        return sharedPreferences.getBoolean(KEY_LIGHT_THEME, true);
    }

    public void setLightTheme(boolean light) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LIGHT_THEME, light);
        editor.apply();
    }

    public boolean getReviewType() {
        return sharedPreferences.getBoolean(KEY_REVIEW_TYPES, true);
    }

    public void setReviewType(boolean isAnime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REVIEW_TYPES, isAnime);
        editor.apply();
    }

    public void setReviewsTip() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REVIEW_TYPE_TIP, false);
        editor.apply();
    }

    public boolean getReviewsTip() {
        return sharedPreferences.getBoolean(KEY_REVIEW_TYPE_TIP, true);
    }
}
