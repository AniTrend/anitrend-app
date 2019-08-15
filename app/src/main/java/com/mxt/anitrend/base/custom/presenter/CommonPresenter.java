package com.mxt.anitrend.base.custom.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.mxt.anitrend.base.custom.recycler.RecyclerScrollListener;
import com.mxt.anitrend.base.interfaces.dao.BoxQuery;
import com.mxt.anitrend.base.interfaces.event.LifecycleListener;
import com.mxt.anitrend.data.DatabaseHelper;
import com.mxt.anitrend.util.ApplicationPref;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by max on 2017/06/09.
 * Base presenter that will act as a template for all presenters
 * All preferences will be referenced from here.
 */

public abstract class CommonPresenter extends RecyclerScrollListener implements LifecycleListener {

    private Bundle bundle;
    private Context context;
    private BoxQuery databaseHelper;
    private ApplicationPref applicationPref;
    
    public CommonPresenter(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public Bundle getParams() {
        if(bundle == null)
            bundle = new Bundle();
        return bundle;
    }

    public void setParams(Bundle bundle) {
        this.bundle = bundle;
    }

    public BoxQuery getDatabase() {
        if(databaseHelper == null)
            databaseHelper = new DatabaseHelper();
        return databaseHelper;
    }

    /**
     * Unregister any listeners from fragments or activities
     */
    @Override
    public void onPause(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(changeListener != null)
            getApplicationPref().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(changeListener);
    }

    /**
     * Register any listeners from fragments or activities
     */
    @Override
    public void onResume(SharedPreferences.OnSharedPreferenceChangeListener changeListener) {
        if(changeListener != null)
            getApplicationPref().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(changeListener);
    }

    /**
     * Destroy any reference which maybe attached to
     * our context
     */
    @Override
    public void onDestroy() {
        bundle = null;
    }

    public ApplicationPref getApplicationPref() {
        if(applicationPref == null)
            applicationPref = new ApplicationPref(context);
        return applicationPref;
    }

    /**
     * Trigger all subscribers that may be listening. This method makes use of sticky broadcasts
     * in case all subscribed listeners were not loaded in time for the broadcast
     * <br/>
     *
     * @param param the object of type T to send
     * @param sticky set true to make sticky post
     */
    public <T> void notifyAllListeners(T param, boolean sticky) {
        if(sticky)
            EventBus.getDefault().postSticky(param);
        else
            EventBus.getDefault().post(param);
    }

    public interface AbstractPresenter <S extends CommonPresenter> {
        S getPresenter();
    }
}
