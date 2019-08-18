package com.mxt.anitrend.base.interfaces.event

/**
 * Created by max on 2017/10/15.
 * Callback for view model to communicate
 * with parent class or activity after a request
 */

interface ResponseCallback {

    fun showError(error: String)

    fun showEmpty(message: String)
}
