package com.mxt.anitrend.base.custom.sheet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;
import com.mxt.anitrend.base.custom.viewmodel.ViewModelBase;
import com.mxt.anitrend.base.interfaces.event.BottomSheetChoice;
import com.mxt.anitrend.base.interfaces.event.BottomSheetListener;
import com.mxt.anitrend.base.interfaces.event.ResponseCallback;
import com.mxt.anitrend.presenter.base.BasePresenter;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.Unbinder;

/**
 * Created by max on 2017/11/02.
 * Custom bottom sheet base implementation
 */

public abstract class BottomSheetBase<T> extends BottomSheetDialogFragment implements BottomSheetListener, ResponseCallback {

    public String TAG;

    protected Unbinder unbinder;

    protected @BindView(R.id.toolbar_title) SingleLineTextView toolbarTitle;
    protected @BindView(R.id.toolbar_state) AppCompatImageView toolbarState;
    protected @BindView(R.id.toolbar_search) AppCompatImageView toolbarSearch;
    protected @BindView(R.id.search_view) MaterialSearchView searchView;

    protected ViewModelBase<T> viewModel;
    protected BottomSheetChoice bottomSheetChoice;

    protected @StringRes int mTitle, mText, mPositive, mNegative;
    protected String searchQuery;

    protected BasePresenter presenter;
    protected BottomSheetBehavior bottomSheetBehavior;
    protected BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
            if(isAlive()) {
                try {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            dismiss();
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            onStateCollapsed();
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            onStateExpanded();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    /**
     * Set up your custom bottom sheet and check for arguments if any
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TAG = this.toString();
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mTitle = getArguments().getInt(KeyUtil.arg_title);
            mText = getArguments().getInt(KeyUtil.arg_text);
            mPositive = getArguments().getInt(KeyUtil.arg_positive_text);
            mNegative = getArguments().getInt(KeyUtil.arg_negative_text);
        }
        presenter = new BasePresenter(getContext());
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     */
    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        toolbarTitle.setText(mTitle);
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(getContext(), R.drawable.ic_keyboard_arrow_down_grey_600_24dp));
        else
            toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(getContext(), R.drawable.ic_close_grey_600_24dp));
        toolbarState.setOnClickListener(view -> {
            switch (bottomSheetBehavior.getState()) {
                case BottomSheetBehavior.STATE_EXPANDED:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                default:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    break;
            }
        });
        toolbarSearch.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(getContext(), R.drawable.ic_search_grey_600_24dp));
        toolbarSearch.setOnClickListener(view -> searchView.showSearch(true));
        searchView.setCursorDrawable(R.drawable.material_search_cursor);
    }

    @Override
    public void onStop() {
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setBottomSheetChoice(BottomSheetChoice bottomSheetChoice) {
        this.bottomSheetChoice = bottomSheetChoice;
    }

    protected boolean isAlive() {
        return isVisible() && !isDetached() || !isRemoving();
    }

    protected void createBottomSheetBehavior(View contentView) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior coordinatorBehavior = layoutParams.getBehavior();

        if (coordinatorBehavior instanceof BottomSheetBehavior) {
            bottomSheetBehavior = (BottomSheetBehavior) coordinatorBehavior;
            bottomSheetBehavior.setPeekHeight(CompatUtil.INSTANCE.dipToPx(KeyUtil.PEEK_HEIGHT));
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
        }
    }

    public boolean closeDialog() {
        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return true;
        }
        return false;
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(unbinder != null)
            unbinder.unbind();
        bottomSheetCallback = null;
    }

    @Override
    public void onStateCollapsed() {
        toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(getContext(), R.drawable.ic_close_grey_600_24dp));
    }

    @Override
    public void onStateExpanded() {
        toolbarState.setImageDrawable(CompatUtil.INSTANCE.getTintedDrawable(getContext(), R.drawable.ic_keyboard_arrow_down_grey_600_24dp));
    }

    /**
     * Builder class for bottom sheet
     */
    public abstract static class BottomSheetBuilder {

        protected Bundle bundle;

        public BottomSheetBuilder() {
            bundle = new Bundle();
        }

        public abstract BottomSheetBase build();

        public BottomSheetBase buildWithCallback(BottomSheetChoice bottomSheetChoice) {
            BottomSheetBase bottomSheetBase = build();
            bottomSheetBase.setBottomSheetChoice(bottomSheetChoice);
            return bottomSheetBase;
        }

        public BottomSheetBuilder setTitle(@StringRes int title) {
            bundle.putInt(KeyUtil.arg_title, title);
            return this;
        }

        public BottomSheetBuilder setPositiveText(@StringRes int positiveText) {
            bundle.putInt(KeyUtil.arg_positive_text, positiveText);
            return this;
        }

        public BottomSheetBuilder setNegativeText(@StringRes int negativeText) {
            bundle.putInt(KeyUtil.arg_negative_text, negativeText);
            return this;
        }
    }

    @Override
    public void showError(String error) {
        Log.e(TAG, error);
    }

    @Override
    public void showEmpty(String message) {
        Log.d(TAG, message);
    }
}
