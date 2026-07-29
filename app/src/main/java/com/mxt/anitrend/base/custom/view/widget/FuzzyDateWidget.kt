package com.mxt.anitrend.base.custom.view.widget

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.DatePicker
import android.widget.FrameLayout
import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.databinding.WidgetFuzzyDateBinding
import com.mxt.anitrend.extension.getLayoutInflater
import com.mxt.anitrend.model.entity.anilist.meta.FuzzyDate
import com.mxt.anitrend.util.date.DateUtil
import java.util.Calendar

class FuzzyDateWidget
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    CustomView,
    View.OnClickListener,
    DatePickerDialog.OnDateSetListener {
    private lateinit var binding: WidgetFuzzyDateBinding
    private var fuzzyDate: FuzzyDate? = null

    val date: FuzzyDate?
        get() = fuzzyDate

    init {
        onInit()
    }

    override fun onInit() {
        binding = WidgetFuzzyDateBinding.inflate(context.getLayoutInflater(), this, true)
        isClickable = true
        isFocusable = true
        setOnClickListener(this)
        binding.fuzzyDateInputLayout.setOnClickListener(this)
        binding.fuzzyDateText.setOnClickListener(this)
    }

    fun setDate(fuzzyDate: FuzzyDate?) {
        this.fuzzyDate = fuzzyDate
        updateDate()
    }

    private fun updateDate() {
        val convertedDate = DateUtil.convertDate(fuzzyDate)
        binding.fuzzyDateText.setText(convertedDate)
    }

    override fun onViewRecycled() = Unit

    override fun onClick(v: View) {
        val calendar = Calendar.getInstance()
        val seedYear = fuzzyDate?.year?.takeIf { it > 0 } ?: calendar.get(Calendar.YEAR)
        val seedMonth = (fuzzyDate?.month?.takeIf { it in 1..12 } ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        val seedDay = fuzzyDate?.day?.takeIf { it in 1..31 } ?: calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            this,
            seedYear,
            seedMonth,
            seedDay,
        )
        datePickerDialog.setButton(
            DialogInterface.BUTTON_NEUTRAL,
            context.getText(R.string.dialog_button_clear),
        ) { _, _ ->
            onDateSet(datePickerDialog.datePicker, 0, -1, 0)
        }
        datePickerDialog.show()
    }

    override fun onDateSet(
        datePicker: DatePicker,
        year: Int,
        month: Int,
        day: Int,
    ) {
        if (fuzzyDate == null) {
            fuzzyDate = FuzzyDate(day, month + 1, year)
        } else {
            fuzzyDate?.setDate(day, month + 1, year)
        }
        updateDate()
    }
}
