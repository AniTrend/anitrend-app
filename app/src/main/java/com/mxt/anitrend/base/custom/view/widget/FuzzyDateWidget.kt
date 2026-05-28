package com.mxt.anitrend.base.custom.view.widget

import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
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

class FuzzyDateWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : this(context, attrs, defStyleAttr)

    override fun onInit() {
        binding = WidgetFuzzyDateBinding.inflate(context.getLayoutInflater(), this, true)
        binding.fuzzyDateView.setOnClickListener(this)
    }

    fun setDate(fuzzyDate: FuzzyDate?) {
        this.fuzzyDate = fuzzyDate
        updateDate()
    }

    private fun updateDate() {
        val convertedDate = DateUtil.convertDate(fuzzyDate)
        binding.fuzzyDateText.text = convertedDate
    }

    override fun onViewRecycled() = Unit

    override fun onClick(v: View) {
        val datePickerDialog = if (fuzzyDate?.isValidDate == true) {
            DatePickerDialog(
                context,
                this,
                fuzzyDate?.year ?: 0,
                (fuzzyDate?.month ?: 1) - 1,
                fuzzyDate?.day ?: 0
            )
        } else {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
        datePickerDialog.setButton(
            DialogInterface.BUTTON_NEUTRAL,
            context.getText(R.string.dialog_button_clear)
        ) { _, _ ->
            onDateSet(datePickerDialog.datePicker, 0, -1, 0)
        }
        datePickerDialog.show()
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        if (fuzzyDate == null)
            fuzzyDate = FuzzyDate(day, month + 1, year)
        else
            fuzzyDate?.setDate(day, month + 1, year)
        updateDate()
    }
}
