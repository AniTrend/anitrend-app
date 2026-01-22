package com.mxt.anitrend.base.interfaces.event

import android.content.SharedPreferences

/**
 * Created by max on 2017/06/14.
 * Should be implemented by presenters
 */
interface LifecycleListener {
    /**
     * Unregister any listeners from fragments or activities
     */
    fun onPause(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?)

    /**
     * Register any listeners from fragments or activities
     */
    fun onResume(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?)

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    fun onDestroy()
}
