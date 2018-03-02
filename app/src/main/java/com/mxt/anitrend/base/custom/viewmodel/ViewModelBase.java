package com.mxt.anitrend.base.custom.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.async.RequestHandler;
import com.mxt.anitrend.base.interfaces.event.ResponseCallback;
import com.mxt.anitrend.base.interfaces.event.RetroCallback;
import com.mxt.anitrend.util.ErrorUtil;
import com.mxt.anitrend.util.KeyUtils;

import io.objectbox.android.ObjectBoxLiveData;
import io.objectbox.query.Query;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by max on 2017/10/14.
 * View model abstraction contains the generic data model
 */

public class ViewModelBase<T> extends ViewModel implements RetroCallback<T> {

    private ObjectBoxLiveData<T> modelBox;
    private MutableLiveData<T> model;
    private ResponseCallback state;

    private RequestHandler<T> mLoader;

    private Bundle bundle;

    private String emptyMessage, errorMessage;

    public void setState(ResponseCallback state) {
        this.state = state;
    }

    public @NonNull MutableLiveData<T> getModel() {
        if(model == null)
            model = new MutableLiveData<>();
        return model;
    }

    public @NonNull ObjectBoxLiveData<T> getModelBox(Query<T> query) {
        if(modelBox == null)
            modelBox = new ObjectBoxLiveData<>(query);
        return modelBox;
    }

    public Bundle setParams(Bundle bundle) {
        this.bundle = new Bundle(bundle);
        return this.bundle;
    }

    public Bundle getParams() {
        if(bundle == null)
            bundle = new Bundle();
        return bundle;
    }

    public void setContext(Context context) {
        if(context != null) {
            emptyMessage = context.getString(R.string.layout_empty_response);
            errorMessage = context.getString(R.string.text_error_request);
        }
    }

    /**
     * Template to make requests for various data types from api, the
     * <br/>
     * @param request_type the type of request to execute
     */
    public void requestData(@KeyUtils.RequestMode int request_type, Context context) {
        mLoader = new RequestHandler<>(getParams(), this, request_type);
        mLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     * <p>
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    @Override
    protected void onCleared() {
        if(mLoader != null && mLoader.getStatus() != AsyncTask.Status.FINISHED)
            mLoader.cancel(true);
        mLoader = null;
        state = null;
        super.onCleared();
    }

    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call     the origination requesting object
     * @param response the response from the network
     */
    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        T container;
        if(response.isSuccessful() && (container = response.body()) != null)
            getModel().setValue(container);
        else if(state != null) {
            state.showError(ErrorUtil.getError(response));
        }
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call      the origination requesting object
     * @param throwable contains information about the error
     */
    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable throwable) {
        if(state != null)
            state.showEmpty(throwable.getMessage());
        throwable.printStackTrace();
    }
}
