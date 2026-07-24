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

/**
 * Created by max on 2017/06/09.
 * Base presenter that will act as a template for all presenters
 * All preferences will be referenced from here.
 */
abstract class CommonPresenter(
    val context: Context,
    val database: BoxQuery,
    val settings: Settings,
) : RecyclerScrollListener(),
    LifecycleListener {
    private var bundle: Bundle? = null

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

    interface AbstractPresenter<S : CommonPresenter> {
        fun getPresenter(): S
    }
}
