package com.mxt.anitrend.base.custom.view.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.View;

import com.mxt.anitrend.R;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class ScoreWidget extends ProgressWidget {

    private float scoreMaximum, scoreCurrent;

    private @KeyUtil.ScoreFormat
    String scoreFormat;

    public ScoreWidget(Context context) {
        super(context);
    }

    public ScoreWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScoreWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScoreWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        super.onInit();
    }

    public void setScoreFormat(@KeyUtil.ScoreFormat String scoreFormat) {
        this.scoreFormat = scoreFormat;
        switch (scoreFormat) {
            case KeyUtil.POINT_10_DECIMAL:
                scoreMaximum = 10.0f;
                break;
            case KeyUtil.POINT_100:
                scoreMaximum = 100;
                break;
            case KeyUtil.POINT_10:
                scoreMaximum = 10;
                break;
            case KeyUtil.POINT_5:
                scoreMaximum = 5;
                break;
            case KeyUtil.POINT_3:
                scoreMaximum = 3;

                break;
        }
        setDefaultDeltaFactor();
        setScoreMaximum();
    }

    /**
     * Sets the default delta value for manipulating scores or progress
     */
    @Override
    protected void setDefaultDeltaFactor() {
        if (scoreFormat != null) {
            switch (scoreFormat) {
                case KeyUtil.POINT_10_DECIMAL:
                    char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
                    binding.progressCurrent.setKeyListener(DigitsKeyListener.getInstance("0123456789" + separator));
                    deltaFactor = 0.1f;
                    break;
                default:
                    deltaFactor = 1;
                    break;
            }
        }
    }

    private void setScoreMaximum() {
        binding.progressMaximum.setVisibility(VISIBLE);
        if (CompatUtil.INSTANCE.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL))
            binding.progressMaximum.setText(String.format(Locale.getDefault(), "/ %.1f", scoreMaximum));
        else
            binding.progressMaximum.setText(String.format(Locale.getDefault(), "/ %d", (int) scoreMaximum));
    }

    public void setScoreCurrent(float scoreCurrent) {
        this.scoreCurrent = scoreCurrent;
        if (CompatUtil.INSTANCE.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL))
            binding.progressCurrent.setText(String.format(Locale.getDefault(), "%.1f", scoreCurrent));
        else
            binding.progressCurrent.setText(String.format(Locale.getDefault(), "%d", (int) scoreCurrent));
    }

    public float getScoreCurrent() {
        return scoreCurrent;
    }

    private boolean boundCheck(float delta) {
        if (scoreMaximum < 1f)
            return delta > -0.1f;
        return delta > -0.1f && delta <= scoreMaximum;
    }

    private void scoreChange(float delta) {
        if (boundCheck(delta)) {
            scoreCurrent = delta;
            if (CompatUtil.INSTANCE.equals(scoreFormat, KeyUtil.POINT_10_DECIMAL))
                binding.progressCurrent.setText(String.format(Locale.getDefault(), "%.1f", scoreCurrent));
            else
                binding.progressCurrent.setText(String.format(Locale.getDefault(), "%d", (int) scoreCurrent));
            binding.progressCurrent.setSelection(binding.progressCurrent.getText().length());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.progress_increment:
                isNotDirectInput = true;
                scoreChange(getRoundedScore(scoreCurrent + deltaFactor));
                isNotDirectInput = false;
                break;
            case R.id.progress_decrement:
                isNotDirectInput = true;
                scoreChange(getRoundedScore(scoreCurrent - deltaFactor));
                isNotDirectInput = false;
                break;
        }
    }

    private float getRoundedScore(float score) {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.US);
        return Float.valueOf(new DecimalFormat("#.#", formatSymbols).format(score));
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (isNotDirectInput)
            return;
        String currentChange = editable.toString();
        float temporaryValue = 0;
        try {
            temporaryValue = !TextUtils.isEmpty(currentChange) ? new DecimalFormat("#.#").parse(currentChange).floatValue() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (scoreFormat.equals(KeyUtil.POINT_10_DECIMAL) && !boundCheck(temporaryValue)) {
            temporaryValue /= 10;
            scoreCurrent = temporaryValue;
            binding.progressCurrent.post(() -> scoreChange(scoreCurrent));
        }
        if (boundCheck(temporaryValue))
            scoreCurrent = temporaryValue;
        else
            binding.progressCurrent.post(() -> scoreChange(scoreCurrent));
    }
}
