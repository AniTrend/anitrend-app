package com.mxt.anitrend.base.interfaces.event;

/**
 * Created by max on 2017/03/06.
 * Used for dialog manager event triggering when an item is updated
 * Only for update or delete actions.
 */
public interface RemoteChangeListener {

    /**
     * Signals that any async tasks which were done in dialog manager are complete
     */
    void onResultSuccess() throws Exception;

    /**
     * Implementation may not be used since errors are handled within the dialog manager
     */
    void onResultFailure() throws Exception;
}
