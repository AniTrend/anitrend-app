package com.mxt.anitrend.base.interfaces.event;

import android.content.SharedPreferences;

/**
 * Created by max on 2017/06/14.
 * Should be implemented by presenters
 */

public interface LifecycleListener {

    /**
     * Unregister any listeners from fragments or activities
     */
    void onPause(SharedPreferences.OnSharedPreferenceChangeListener changeListener);

    /**
     * Register any listeners from fragments or activities
     */
    void onResume(SharedPreferences.OnSharedPreferenceChangeListener changeListener);

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    void onDestroy();
}
