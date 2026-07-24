package com.mxt.anitrend.presenter.widget

import android.content.Context
import com.mxt.anitrend.base.custom.async.RequestHandler
import com.mxt.anitrend.base.interfaces.dao.BoxQuery
import com.mxt.anitrend.base.interfaces.event.RetroCallback
import com.mxt.anitrend.presenter.base.BasePresenter
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings
import java.util.Locale

/**
 * Created by max on 2017/10/31.
 */
class WidgetPresenter<T>(
    context: Context,
    boxQuery: BoxQuery,
    settings: Settings,
) : BasePresenter(context, boxQuery, settings) {
    private var loader: RequestHandler<T>? = null

    /**
     * Template to make requests for various data types from api, the
     *
     * @param requestType the type of request to execute
     */
    fun requestData(
        @KeyUtil.RequestType requestType: Int,
        context: Context,
        callback: RetroCallback<T>,
    ) {
        loader =
            RequestHandler(params, callback, requestType).also {
                it.execute(context)
            }
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    override fun onDestroy() {
        loader?.cancel()
        loader = null
        super.onDestroy()
    }

    companion object {
        const val CONTENT_STATE = 0
        const val LOADING_STATE = 1

        fun convertToText(count: Int): String = String.format(Locale.getDefault(), " %d ", count)

        fun valueFormatter(size: Int): String {
            if (size != 0) {
                return if (size > 1000) {
                    String.format(Locale.getDefault(), "%.1f K", size / 1000f)
                } else {
                    size.toString()
                }
            }
            return "0"
        }
    }
}
