package com.mxt.anitrend.view.sheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.sheet.BottomSheetBase;
import com.mxt.anitrend.binding.RichMarkdownExtensionsKt;
import com.mxt.anitrend.databinding.BottomSheetMessageBinding;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

/**
 * Created by max on 2017/11/03.
 * Displays messages with two buttons
 */

public class BottomSheetMessage extends BottomSheetBase implements View.OnClickListener {

    private BottomSheetMessageBinding binding;

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
        binding = BottomSheetMessageBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getActivity()));
        dialog.setContentView(binding.getRoot());
        bindToolbarViews(binding.getRoot());
        createBottomSheetBehavior(binding.getRoot());
        binding.bottomPositive.setOnClickListener(this);
        binding.bottomNegative.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        RichMarkdownExtensionsKt.basicText(binding.bottomText, getString(mText));
        if(mPositive != 0)
            binding.bottomPositive.setText(mPositive);
        else
            binding.bottomPositive.setVisibility(View.GONE);

        if(mNegative != 0)
            binding.bottomNegative.setText(mNegative);
        else
            binding.bottomNegative.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
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
