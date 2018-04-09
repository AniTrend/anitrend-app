package com.mxt.anitrend.presenter.widget;

import android.content.Context;
import android.os.AsyncTask;

import com.mxt.anitrend.base.custom.async.RequestHandler;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.KeyUtil;

import java.util.Locale;

/**
 * Created by max on 2017/10/31.
 */

public class WidgetPresenter<T> extends BasePresenter {

    private RequestHandler<T> mLoader;

    public static final int CONTENT_STATE = 0, LOADING_STATE = 1;

    public WidgetPresenter(Context context) {
        super(context);
    }

    /**
     * Template to make requests for various data types from api, the
     * <br/>
     * @param request_type the type of request to execute
     */
    public void requestData(@KeyUtil.RequestType int request_type, Context context, RetroCallback<T> callback) {
        mLoader = new RequestHandler<>(getParams(), callback, request_type);
        mLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    @Override
    public void onDestroy() {
        if(mLoader != null && mLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mLoader.cancel(true);
            mLoader = null;
        }
        super.onDestroy();
    }

    public static String convertToText(int count) {
        return String.format(Locale.getDefault()," %d ", count);
    }

    public static String valueFormatter(int size){
        if(size != 0){
            return size > 1000? String.format(Locale.getDefault(),"%.1f K", (float)size/1000): String.valueOf(size);
        }
        return "0";
    }
}
