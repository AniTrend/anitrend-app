package com.mxt.anitrend.base.custom.view.widget.bottomsheet;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.event.SheetStateChangeListener;
import com.mxt.anitrend.util.KeyUtils;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.PatternMatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/08/30.
 */

public class BottomSheetShare extends BottomSheet implements SheetStateChangeListener, AdapterView.OnItemSelectedListener {

    @BindView(R.id.sheet_positive)
    AppCompatButton mPositive;
    @BindView(R.id.sheet_negative)
    AppCompatButton mNegative;
    @BindView(R.id.sheet_state)
    TextView mState;
    @BindView(R.id.sheet_share_post_type)
    Spinner mContentType;

    private String result;
    private String subject, text;
    private SheetActionCallback mCallback;

    private static BottomSheetShare newInstance() {
        return new BottomSheetShare();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog mSheet = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_share, null);
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
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.post_share_types, R.layout.adapter_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mContentType.setAdapter(adapter);

        mContentType.setOnItemSelectedListener(this);
        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onStateCollapsed() {
        mState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_up_grey_600_18dp,0,0,0);
    }

    @Override
    public void onStateExpanded() {
        mState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_grey_600_18dp,0,0,0);
    }

    @OnClick({R.id.sheet_state, R.id.sheet_positive, R.id.sheet_negative})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sheet_positive:
                if(result == null)
                    result = text;
                if(subject != null)
                    mCallback.onPositive(bottomSheetBehavior, String.format("%s %s", result, subject));
                else
                    mCallback.onPositive(bottomSheetBehavior, result);
                break;
            case R.id.sheet_negative:
                mCallback.onNegative(bottomSheetBehavior);
                break;
            case R.id.sheet_state:
                switch (bottomSheetBehavior.getState()) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                }
                break;
        }
    }

    public void setCallback(SheetActionCallback mCallback) {
        this.mCallback = mCallback;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, @KeyUtils.ShareType int position, long id) {
        switch (position) {
            case KeyUtils.IMAGE_TYPE:
                result = MarkDown.convertImage(text);
                break;
            case KeyUtils.LINK_TYPE:
                result = MarkDown.convertLink(text);
                break;
            case KeyUtils.WEBM_TYPE:
                result = MarkDown.convertVideo(text);
                break;
            case KeyUtils.YOUTUBE_TYPE:
                result = MarkDown.convertYoutube(text);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class Builder {

        private SheetActionCallback sheetButtonEvents;
        private String subject, text;

        public BottomSheetShare build() {
            BottomSheetShare instance = newInstance();
            instance.setCallback(sheetButtonEvents);
            instance.setSubject(subject);
            instance.setText(text);
            return instance;
        }

        public Builder setButtonEvents(SheetActionCallback sheetButtonEvents) {
            this.sheetButtonEvents = sheetButtonEvents;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

    }

    public interface SheetActionCallback {
        void onPositive(BottomSheetBehavior sheetBehavior, String result);
        void onNegative(BottomSheetBehavior sheetBehavior);
    }
}
