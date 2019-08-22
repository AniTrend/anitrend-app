package com.mxt.anitrend.base.custom.view.widget;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.databinding.WidgetFuzzyDateBinding;
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.date.DateUtil;

import java.util.Calendar;

public class FuzzyDateWidget extends FrameLayout implements CustomView, View.OnClickListener, DatePickerDialog.OnDateSetListener{

    private WidgetFuzzyDateBinding binding;

    private @Nullable FuzzyDate fuzzyDate;

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
        binding = WidgetFuzzyDateBinding.inflate(CompatUtil.INSTANCE.getLayoutInflater(getContext()),this, true);
        binding.setOnClick(this);
    }

    public void setDate( @Nullable FuzzyDate fuzzyDate) {
        this.fuzzyDate = fuzzyDate;
        updateDate();
    }

    private void updateDate() {
        String convertedDate = DateUtil.INSTANCE.convertDate(fuzzyDate);
        binding.setModel(convertedDate);
        binding.executePendingBindings();
    }

    public @Nullable FuzzyDate getDate() {
        return fuzzyDate;
    }

    @Override
    public void onViewRecycled() {

    }

    @Override
    public void onClick(View v) {
        DatePickerDialog datePickerDialog;
        if(fuzzyDate != null && fuzzyDate.isValidDate()) {
            datePickerDialog = new DatePickerDialog(getContext(), this,
                    fuzzyDate.getYear(), fuzzyDate.getMonth() - 1, fuzzyDate.getDay());
        } else {
            Calendar calendar = Calendar.getInstance();
            datePickerDialog = new DatePickerDialog(getContext(), this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getContext().getText(R.string.dialog_button_clear), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onDateSet(datePickerDialog.getDatePicker(), 0, -1, 0);
            }
        });
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if(fuzzyDate == null)
            fuzzyDate = new FuzzyDate(day, month + 1, year);
        else
            fuzzyDate.setDate(day, month + 1, year);
        updateDate();
    }
}