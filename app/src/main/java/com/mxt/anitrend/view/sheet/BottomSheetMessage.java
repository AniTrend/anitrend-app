package com.mxt.anitrend.view.sheet;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView;
import com.mxt.anitrend.binding.RichMarkdownExtensionsKt;
import com.mxt.anitrend.util.KeyUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by max on 2017/11/03.
 * Displays messages with two buttons
 */

public class BottomSheetMessage extends BottomSheetBase implements View.OnClickListener {

    protected @BindView(R.id.bottom_text) RichMarkdownTextView bottom_text;
    protected @BindView(R.id.bottom_positive) AppCompatButton bottom_positive;
    protected @BindView(R.id.bottom_negative) AppCompatButton bottom_negative;

    public static BottomSheetMessage newInstance(Bundle bundle) {
        BottomSheetMessage fragment = new BottomSheetMessage();
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Setup your view un-binder here as well as inflating other views as needed
     * into your view stub
     *
     * @param savedInstanceState
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_message, null);
        dialog.setContentView(contentView);
        unbinder = ButterKnife.bind(this, dialog);
        createBottomSheetBehavior(contentView);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        RichMarkdownExtensionsKt.basicText(bottom_text, getString(mText));
        if(mPositive != 0)
            bottom_positive.setText(mPositive);
        else
            bottom_positive.setVisibility(View.GONE);

        if(mNegative != 0)
            bottom_negative.setText(mNegative);
        else
            bottom_negative.setVisibility(View.GONE);
    }

    @Override @OnClick({R.id.bottom_positive, R.id.bottom_negative})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bottom_positive:
                if(bottomSheetChoice != null)
                    bottomSheetChoice.onPositiveButton();
                closeDialog();
                break;
            case R.id.bottom_negative:
                if(bottomSheetChoice != null)
                    bottomSheetChoice.onNegativeButton();
                closeDialog();
                break;
        }
    }

    public static class Builder extends BottomSheetBuilder {

        @Override
        public BottomSheetBase build() {
            return newInstance(bundle);
        }

        public BottomSheetBuilder setText(@StringRes int text) {
            bundle.putInt(KeyUtil.arg_text, text);
            return this;
        }
    }
}
