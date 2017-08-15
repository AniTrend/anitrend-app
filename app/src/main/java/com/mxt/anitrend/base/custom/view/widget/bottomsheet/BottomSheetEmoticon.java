package com.mxt.anitrend.base.custom.view.widget.bottomsheet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.adapter.recycler.details.EmoticonAdapter;
import com.mxt.anitrend.base.custom.recycler.StatefulRecyclerView;
import com.mxt.anitrend.base.custom.view.widget.emoji4j.Emoji;
import com.mxt.anitrend.base.interfaces.event.InteractionListener;
import com.mxt.anitrend.base.interfaces.event.SheetStateChangeListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/04/23.
 */

public class BottomSheetEmoticon extends BottomSheet implements SheetStateChangeListener {

    protected final static String ARG_CONTENT = "arg_model";
    protected final static String ARG_TITLE = "arg_title";


    @BindView(R.id.em_title) TextView mTitle;
    @BindView(R.id.em_state) TextView mState;
    @BindView(R.id.generic_recycler) StatefulRecyclerView recyclerView;

    private List<Emoji> mList;
    private String Title;

    private InteractionListener emoticonListener;

    public static BottomSheet newInstance(String title, List<Emoji> adapter) {
        BottomSheet bottomSheetDialogFragment = new BottomSheetEmoticon();

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
            Title = args.getString(ARG_TITLE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog mSheet = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_emoticons, null);
        mSheet.setContentView(contentView);

        unbinder = ButterKnife.bind(this, mSheet);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            bottomSheetBehavior = (BottomSheetBehavior) behavior;
            bottomSheetBehavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        onPostCreate(this);
        return mSheet;
    }

    @Override
    public void onStart() {
        super.onStart();
        mState.setOnClickListener(this);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(getResources().getInteger(R.integer.list_col_emoticon), StaggeredGridLayoutManager.HORIZONTAL));
        mTitle.setText(Title);
        recyclerView.setAdapter(new EmoticonAdapter(mList, getContext(), new InteractionListener() {
            @Override
            public void onItemClick(int index) {
                emoticonListener.onItemClick(index);
                dismiss();
            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (bottomSheetBehavior.getState()) {
            case BottomSheetBehavior.STATE_COLLAPSED:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
        }
    }

    @Override
    public void onStateCollapsed() {
        mState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_up_grey_600_18dp,0,0,0);
    }

    @Override
    public void onStateExpanded() {
        mState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_grey_600_18dp,0,0,0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InteractionListener) {
            emoticonListener = (InteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InteractionListener");
        }
    }

    /**
     * Remove dialog.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emoticonListener = null;
    }
}
