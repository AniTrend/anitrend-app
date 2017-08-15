package com.mxt.anitrend.viewmodel.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mxt.anitrend.base.custom.async.SortHelper;
import com.mxt.anitrend.presenter.CommonPresenter;
import com.mxt.anitrend.presenter.index.FragmentPresenter;

import butterknife.Unbinder;

/**
 * Created by max on 2017/04/09.
 * A basic implementation of some default fragments.
 * <br/><br/>
 *
 * DefaultFragment <T extends Parcelable> is the model type you wish to use for your fragment implementation,
 * if you have more than one implementation then you are responsible for state
 * management of the other model types!
 * <br/><br/>
 *
 * This class includes the following props:
 * model: Model of type T
 * unbinder: Unbinder from ButterKnife
 * mPresenter: Fragment presenter
 */
public abstract class DefaultFragment <T extends Parcelable> extends Fragment {

    protected final static String ARG_KEY = "arg_data";

    private final String KEY_MODEL_STATE = "model_key";

    protected CommonPresenter mPresenter;
    protected Unbinder unbinder;
    protected T model;

    protected SortHelper<T> mSorter;
    /**
     * Override if you need to include extra functionality into the method,
     * the method will get the arguments from the from your bundle and into
     * the model followed by initialization of your presenter
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments().containsKey(ARG_KEY))
            model = getArguments().getParcelable(ARG_KEY);
        mPresenter = new FragmentPresenter(getContext());
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        if(mPresenter != null)
            mPresenter.onPause();
    }

    /**
     * Override as normal, the saving of the model is also managed for
     * you so no need to save it.
     *
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_MODEL_STATE, model);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null)
            model = savedInstanceState.getParcelable(KEY_MODEL_STATE);
    }

    /**
     * No need to call ButterKnife.unbind()
     * method is already called for you
     */
    @Override
    public void onDestroyView() {
        if(mPresenter != null)
            mPresenter.onDestroy();
        super.onDestroyView();
        unbinder.unbind();
    }

    protected boolean isAlive() {
        return isVisible() && (!isDetached() || !isRemoving());
    }

    /**
     * Is automatically called in the @onStart Method
     */
    protected abstract void updateUI();

}
