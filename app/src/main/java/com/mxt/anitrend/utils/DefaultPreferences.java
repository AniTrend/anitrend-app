package com.mxt.anitrend.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mxt.anitrend.R;

/**
 * Created by Maxwell on 1/23/2017.
 */
public class DefaultPreferences {

    //300 seconds -> 5 minutes
    public static final int MINIMUM_SYNC_TIME = 300;
    private Context context;
    private SharedPreferences sharedPreferences;

    public DefaultPreferences(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences getPreferences() {
        return sharedPreferences;
    }

    //Returns the id of the startup page
    public int getStartupPage() {
        String id = sharedPreferences.getString(context.getString(R.string.pref_key_startup_page), "4");
        switch (id){
            case "0":
                return R.id.nav_home;
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

    public boolean isAutoIncrement() {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_auto_increment), false);
    }

    //Returns amount of time in seconds
    public int getSyncTime() {
        return Integer.valueOf(sharedPreferences.getString(context.getString(R.string.pref_key_sync_frequency), "30")) * 60;
    }

    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_new_message_notifications), true);
    }

    public String getNotificationsSound() {
        return sharedPreferences.getString(context.getString(R.string.pref_key_ringtone), "DEFAULT_SOUND");
    }
}
