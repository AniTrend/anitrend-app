package com.mxt.anitrend.base.custom.view.widget.bottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.user.UserListAdapter;
import com.mxt.anitrend.api.model.UserSmall;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.interfaces.event.SheetStateChangeListener;
import com.mxt.anitrend.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/04/19.
 */

public class BottomSheetUsers extends BottomSheet implements SheetStateChangeListener {

    protected final static String ARG_CONTENT = "arg_model";
    protected final static String ARG_TITLE = "arg_title";


    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;

    private final float PEEK_HEIGHT = 220f;

    private List<UserSmall> mList;
    private String mTitle;


    public static BottomSheet newInstance(String title, List<UserSmall> adapter) {
        BottomSheet bottomSheetDialogFragment = new BottomSheetUsers();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);

        args.putParcelableArrayList(ARG_CONTENT, (ArrayList<? extends Parcelable>) adapter);
        bottomSheetDialogFragment.setArguments(args);

        return bottomSheetDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            mList = args.getParcelableArrayList(ARG_CONTENT);
            mTitle = args.getString(ARG_TITLE);
        }
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        //dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog mSheet = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_recycler, null);
        mSheet.setContentView(contentView);

        unbinder = ButterKnife.bind(this, mSheet);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            bottomSheetBehavior = (BottomSheetBehavior) behavior;
            bottomSheetBehavior.setPeekHeight(ScreenUtil.dip2px(getContext(), PEEK_HEIGHT));
            bottomSheetBehavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        mToolbar.setNavigationOnClickListener(this);

        onPostCreate(this);
        return mSheet;
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);
        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_col_size));
        recyclerView.setLayoutManager(mLayoutManager);
        mToolbar.setTitle(mTitle);
        recyclerView.setAdapter(new UserListAdapter(mList, getContext()));
    }

    @Override
    public void onClick(View v) {
        switch (bottomSheetBehavior.getState()) {
            case BottomSheetBehavior.STATE_EXPANDED:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            default:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
        }
    }

    @Override
    public void onStateCollapsed() {
        mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
    }

    @Override
    public void onStateExpanded() {
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_down_white_24dp);
    }
}