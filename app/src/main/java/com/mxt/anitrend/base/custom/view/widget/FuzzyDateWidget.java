package com.mxt.anitrend.base.custom.view.widget;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.data.converter.FuzzyDateConverter;
import com.mxt.anitrend.databinding.WidgetFuzzyDateBinding;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.util.CompatUtil;

import java.util.Calendar;
import java.util.Locale;

public class FuzzyDateWidget extends FrameLayout implements CustomView, View.OnClickListener, DatePickerDialog.OnDateSetListener{

    private WidgetFuzzyDateBinding binding;

    private FuzzyDate fuzzyDate;

    public FuzzyDateWidget(Context context) {
        super(context);
        onInit();
    }

    public FuzzyDateWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public FuzzyDateWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FuzzyDateWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInit();
    }

    @Override
    public void onInit() {
        binding = WidgetFuzzyDateBinding.inflate(CompatUtil.getLayoutInflater(getContext()),this, true);
        binding.setOnClick(this);
    }

    public void setDate(FuzzyDate fuzzyDate) {
        this.fuzzyDate = fuzzyDate;
        updateDate();
    }

    private void updateDate(){
        binding.fuzzyDateDay.setText(String.valueOf(fuzzyDate.getDay()));
        binding.fuzzyDateMonth.setText(String.valueOf(fuzzyDate.getMonth()));
        binding.fuzzyDateYear.setText(String.valueOf(fuzzyDate.getYear()));
    }

    public FuzzyDate getDate() {
        return fuzzyDate;
    }

    @Override
    public void onViewRecycled() {

    }

    @Override
    public void onClick(View v) {
        Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(), this, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
            fuzzyDate.setDate(d, m+1, y);
            updateDate();
    }
}


