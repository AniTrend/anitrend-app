package com.mxt.anitrend.base.interfaces.event;

/**
 * Created by max on 2017/10/15.
 * Callback for view model to communicate
 * with parent class or activity after a request
 */

public interface ResponseCallback {

    void showError(String error);

    void showEmpty(String message);
}
