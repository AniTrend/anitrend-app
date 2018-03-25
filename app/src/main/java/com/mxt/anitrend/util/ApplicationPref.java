package com.mxt.anitrend.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.StyleRes;

import com.mxt.anitrend.R;

/**
 * Created by max on 2017/09/16.
 * Application preferences
 */

public class ApplicationPref {

    private Context context;

    /** Base Application Values */
    private final String KEY_FRESH_INSTALL = "KEY_FRESH_INSTALL";
    private final String KEY_AUTHENTICATED = "KEY_AUTHENTICATED";

    /** Application Base Options */
    private final String KEY_LIGHT_THEME = "KEY_LIGHT_THEME";

    private SharedPreferences sharedPreferences;

    public ApplicationPref(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
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

    public void saveSeasonYear(int year) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KeyUtils.arg_seasonYear, year);
        editor.apply();
    }

    public int getSeasonYear() {
        return sharedPreferences.getInt(KeyUtils.arg_seasonYear, DateUtil.getYear());
    }


    public boolean shouldShowTipFor(@KeyUtils.TapTargetType String tipType) {
        return sharedPreferences.getBoolean(tipType, true);
    }

    public void disableTipFor(@KeyUtils.TapTargetType String tipType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(tipType, false);
        editor.apply();
    }

    public void saveOrder(@KeyUtils.SortOrderType String order) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KeyUtils.arg_order, order);
        editor.apply();
    }

    public @KeyUtils.SortOrderType String getOrder() {
        return sharedPreferences.getString(KeyUtils.arg_order, KeyUtils.DESC);
    }
}
