package com.mxt.anitrend.base.custom.presenter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.base.interfaces.event.LifecycleListener
import com.mxt.anitrend.data.DatabaseHelper
import com.mxt.anitrend.extension.KoinExt
import com.mxt.anitrend.util.Settings
import org.greenrobot.eventbus.EventBus

/**
 * Created by max on 2017/06/09.
 * Base presenter that will act as a template for all presenters
 * All preferences will be referenced from here.
 */
abstract class CommonPresenter(
    val context: Context,
) : RecyclerScrollListener(),
    LifecycleListener {
    private var bundle: Bundle? = null
    private var databaseHelper: BoxQuery? = null
    private var settingsCache: Settings? = null

    var params: Bundle
        get() {
            if (bundle == null) {
                bundle = Bundle()
            }
            return requireNotNull(bundle)
        }
        set(value) {
            bundle = value
        }

    val database: BoxQuery
        get() {
            if (databaseHelper == null) {
                databaseHelper = DatabaseHelper()
            }
            return requireNotNull(databaseHelper)
        }

    val settings: Settings
        get() {
            if (settingsCache == null) {
                settingsCache = KoinExt.get(Settings::class.java)
            }
            return requireNotNull(settingsCache)
        }

    /**
     * Unregister any listeners from fragments or activities
     */
    override fun onPause(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        changeListener?.let { settings.unregisterOnSharedPreferenceChangeListener(it) }
    }

    /**
     * Register any listeners from fragments or activities
     */
    override fun onResume(changeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        changeListener?.let { settings.registerOnSharedPreferenceChangeListener(it) }
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    override fun onDestroy() {
        bundle = null
    }

    /**
     * Trigger all subscribers that may be listening. This method makes use of sticky broadcasts
     * in case all subscribed listeners were not loaded in time for the broadcast
     *
     * @param param the object of type T to send
     * @param sticky set true to make sticky post
     */
    fun <T> notifyAllListeners(
        param: T,
        sticky: Boolean,
    ) {
        if (sticky) {
            EventBus.getDefault().postSticky(param)
        } else {
            EventBus.getDefault().post(param)
        }
    }

    interface AbstractPresenter<S : CommonPresenter> {
        fun getPresenter(): S
    }
}
