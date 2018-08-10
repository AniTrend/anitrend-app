package com.mxt.anitrend.base.custom.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetProgressBinding;
import com.mxt.anitrend.util.CompatUtil;

import java.util.Locale;

public class ProgressWidget extends FrameLayout implements CustomView, View.OnClickListener, TextWatcher {

    private int progressMaximum, progressCurrent;

    protected WidgetProgressBinding binding;
    protected boolean isNotDirectInput;
    protected float deltaFactor;

    public ProgressWidget(Context context) {
        super(context);
        onInit();
    }

    public ProgressWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public ProgressWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        binding = WidgetProgressBinding.inflate(CompatUtil.getLayoutInflater(getContext()),this, true);
        binding.progressCurrent.setTextColor(CompatUtil.getColorFromAttr(getContext(), R.attr.contentColor));
        binding.progressMaximum.setVisibility(GONE);
        binding.setOnClick(this);
        setDefaultDeltaFactor();
    }

    /**
     * Sets the default delta value for manipulating scores or progress
     */
    protected void setDefaultDeltaFactor() {
        deltaFactor = 1;
    }

    public void setProgressMaximum(int progressMaximum) {
        this.progressMaximum = progressMaximum;
        binding.progressMaximum.setVisibility(VISIBLE);
        binding.progressMaximum.setText(String.format(Locale.getDefault(),"/ %d",progressMaximum));
    }

    public void setProgressCurrent(int progressCurrent) {
        this.progressCurrent = progressCurrent;
        binding.progressCurrent.setText(String.valueOf(progressCurrent));
    }

    public int getProgressMaximum() {
        return progressMaximum;
    }

    public int getProgressCurrent() {
        return progressCurrent;
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {
        binding.progressCurrent.removeTextChangedListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        binding.progressCurrent.addTextChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        onViewRecycled();
        super.onDetachedFromWindow();
    }

    private boolean boundCheck(int delta) {
        if(progressMaximum < 1)
            return delta > -1;
        return delta > -1 && delta <= progressMaximum;
    }

    private void progressChange(int delta) {
        if (boundCheck(delta)) {
            progressCurrent = delta;
            binding.progressCurrent.setText(String.valueOf(progressCurrent));
            binding.progressCurrent.setSelection(binding.progressCurrent.getText().length());
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.progress_increment:
                isNotDirectInput = true;
                progressChange(progressCurrent + (int)deltaFactor);
                isNotDirectInput = false;
                break;
            case R.id.progress_decrement:
                isNotDirectInput = true;
                progressChange(progressCurrent - (int)deltaFactor);
                isNotDirectInput = false;
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (isNotDirectInput)
            return;
        String currentChange = editable.toString();
        int temporaryValue = !TextUtils.isEmpty(currentChange) ? Integer.parseInt(currentChange) : 0;
        if(boundCheck(temporaryValue))
            progressCurrent = temporaryValue;
        else
            binding.progressCurrent.post(() -> progressChange(progressCurrent));
    }
}
