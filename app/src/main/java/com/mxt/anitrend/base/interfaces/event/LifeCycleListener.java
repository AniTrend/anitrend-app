package com.mxt.anitrend.base.interfaces.event;

/**
 * Created by max on 2017/07/29.
 */

public interface LifeCycleListener {

    /**
     * Unregister any listeners from fragments or activities
     */
    void onPause();

    /**
     * Register any listeners from fragments or activities
     */
    void onResume();

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    void onDestroy();
}
