package com.mxt.anitrend.base.interfaces.event;

/**
 * Created by max on 2017/03/08.
 */

public interface ApplicationInitListener {
    /**
     * First time installation, or data cleared
     */
    void onNewInstallation();
    /**
     * The application version has been updated
     */
    void onUpdatedVersion();
    /**
     * New notification count
     */
    void onNewNotification();
    /**
     * Nothing new here, just init finished and no new version
     */
    void onNormalStart();
}
