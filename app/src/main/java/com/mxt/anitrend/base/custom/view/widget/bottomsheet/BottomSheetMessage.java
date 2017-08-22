package com.mxt.anitrend.base.custom.view.widget.bottomsheet;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.service.ServiceScheduler;
import com.mxt.anitrend.base.interfaces.event.SheetStateChangeListener;
import com.mxt.anitrend.view.base.activity.SplashActivity;
import com.mxt.anitrend.api.call.RepoModel;
import com.mxt.anitrend.api.service.ServiceGenerator;
import com.mxt.anitrend.base.custom.service.DownloaderService;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by max on 2017/04/15.
 */
public class BottomSheetMessage extends BottomSheet implements SheetStateChangeListener {

    private final static String ARG_TEXT = "arg_text";
    private final static String ARG_TITLE = "arg_title";
    private final static String ARG_POS = "arg_pos";
    private final static String ARG_NEG = "arg_neg";
    private final static String ARG_CALL = "arg_callback";

    private final String TITLE_KEY = "title_key";
    private final String TEXT_KEY = "text_key";
    private final String POS_KEY = "pos_key";
    private final String NEG_KEY = "neg_key";
    private final String CALL_KEY = "callback_key";

    @BindView(R.id.msg_title) TextView mTitle;
    @BindView(R.id.msg_text) TextView mText;
    @BindView(R.id.msg_state) TextView mState;

    @BindView(R.id.msg_positive) AppCompatButton mPositive;
    @BindView(R.id.msg_negative) AppCompatButton mNegative;

    @StringRes int title, text, positive, negative;

    private int mCallback;

    private static BottomSheetMessage newInstance(@StringRes int title, @StringRes int text, @StringRes int positive, @StringRes int negative, int call) {
        Bundle args = new Bundle();
        BottomSheetMessage mBottomMessage = new BottomSheetMessage();
        args.putInt(ARG_TITLE, title);
        args.putInt(ARG_TEXT, text);
        args.putInt(ARG_POS, positive);
        args.putInt(ARG_NEG, negative);
        args.putInt(ARG_CALL, call);
        mBottomMessage.setArguments(args);
        return mBottomMessage;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            title = args.getInt(ARG_TITLE,0);
            text = args.getInt(ARG_TEXT, 0);
            positive = args.getInt(ARG_POS, 0);
            negative = args.getInt(ARG_NEG, 0);
            mCallback = args.getInt(ARG_CALL);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog mSheet = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_message, null);
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

        if(negative == 0)
            mNegative.setVisibility(View.GONE);
        else {
            mNegative.setOnClickListener(this);
            mNegative.setText(negative);
        }

        if(positive == 0)
            mPositive.setVisibility(View.GONE);
        else {
            mPositive.setOnClickListener(this);
            mPositive.setText(positive);
        }

        mTitle.setText(title);
        mText.setMovementMethod(LinkMovementMethod.getInstance());
        mText.setText(Html.fromHtml(getString(text)));
        if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TITLE_KEY, title);
        outState.putInt(TEXT_KEY, text);
        outState.putInt(POS_KEY, positive);
        outState.putInt(NEG_KEY, negative);
        outState.putInt(CALL_KEY, mCallback);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            title = savedInstanceState.getInt(TITLE_KEY,0);
            text = savedInstanceState.getInt(TEXT_KEY, 0);
            positive = savedInstanceState.getInt(POS_KEY, 0);
            negative = savedInstanceState.getInt(NEG_KEY, 0);
            mCallback = savedInstanceState.getInt(CALL_KEY);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.msg_positive:
                switch (mCallback) {
                    case SheetButtonEvents.CALLBACK_UPDATE_CHECKER:
                        if(mPresenter.getAppPrefs().getRepoVersions().checkAgainstCurrent()) {
                            mPresenter.createAlerter(getActivity(), getString(R.string.title_download_info), getString(R.string.text_download_info),
                                    R.drawable.ic_cloud_download_white_24dp, R.color.colorStateBlue);

                            new DownloaderService(getContext()).startUpdateDownload(RepoModel.DOWNLOAD_LINK,
                                    String.format("AniTrend v%s.apk", mPresenter.getAppPrefs().getRepoVersions().getVersion()));
                        }
                        else
                            mPresenter.createAlerter(getActivity(), getString(R.string.title_update_info), getString(R.string.app_no_date),
                                    R.drawable.ic_done_all_white_24dp, R.color.colorStateGreen);
                        break;
                    case SheetButtonEvents.CALLBACK_SIGN_OUT:
                        ServiceGenerator.authStateChange(getContext());
                        new ServiceScheduler(getContext()).cancelJob();
                        mPresenter.getAppPrefs().setUserDeactivated();
                        Intent intent = new Intent(getActivity(), SplashActivity.class);
                        getActivity().finish();
                        startActivity(intent);
                        break;
                    case SheetButtonEvents.CALLBACK_APP_INTRO:
                        mPresenter.getAppPrefs().setMainTip();
                        break;
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
            case R.id.msg_negative:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
            case R.id.msg_state:
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


    public static class Builder {

        private @StringRes int title;
        private @StringRes int text;
        private @StringRes int positive;
        private @StringRes int negative;
        private int callback;

        public BottomSheetMessage build() {
            return newInstance(title, text, positive, negative, callback);
        }

        public Builder setTitle(@StringRes int title) {
            this.title = title;
            return this;
        }

        public Builder setText(@StringRes int text) {
            this.text = text;
            return this;
        }

        public Builder setPositive(@StringRes int positive) {
            this.positive = positive;
            return this;
        }

        public Builder setNegative(@StringRes int negative) {
            this.negative = negative;
            return this;
        }

        public Builder setCallback(int callback) {
            this.callback = callback;
            return this;
        }
    }

}
