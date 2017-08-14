package com.mxt.anitrend.base.custom.view.widget.bottomsheet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import com.mxt.anitrend.base.interfaces.event.SheetStateChangeListener;
import com.mxt.anitrend.presenter.index.FragmentPresenter;

import butterknife.Unbinder;

/**
 * Created by max on 2017/04/15.
 */

public abstract class BottomSheet extends BottomSheetDialogFragment implements View.OnClickListener{

    protected Unbinder unbinder;

    protected SheetStateChangeListener mSheetStateChangeListener;

    protected BottomSheetBehavior bottomSheetBehavior;

    protected FragmentPresenter mPresenter;

    protected BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(isVisible() && !isDetached() || !isRemoving())
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        dismiss();
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mSheetStateChangeListener.onStateCollapsed();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mSheetStateChangeListener.onStateExpanded();
                        break;
                }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    protected void onPostCreate(SheetStateChangeListener mSheetStateChangeListener) {
        this.mSheetStateChangeListener = mSheetStateChangeListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new FragmentPresenter(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(unbinder != null)
            unbinder.unbind();
    }

    public interface SheetButtonEvents {
        int CALLBACK_UPDATE_CHECKER = 0;
        int CALLBACK_SIGN_OUT = 1;
        int CALLBACK_APP_INTRO = 2;
    }
}
