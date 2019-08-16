package com.mxt.anitrend.util

import android.content.Context
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.mxt.anitrend.R

/**
 * Created by max on 2018/02/24.
 */

class ChartUtil {

    class StepXAxisFormatter<K> : ValueFormatter() {

        private lateinit var dataModel: List<K>
        private lateinit var chartBase: BarLineChartBase<*>

        fun setChartBase(chartBase: BarLineChartBase<*>): StepXAxisFormatter<*> {
            this.chartBase = chartBase
            return this
        }

        fun setDataModel(dataModel: List<K>): StepXAxisFormatter<K> {
            this.dataModel = dataModel
            return this
        }

        /**
         * Called when a value from an axis is to be formatted
         * before being drawn. For performance reasons, avoid excessive calculations
         * and memory allocations inside this method.
         *
         * @param value the value to be formatted
         * @param axis  the axis the value belongs to
         * @return formatted label value
         */
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val index = value.toInt()
            val model = dataModel[index]
            return model.toString()
        }

        fun build(context: Context) {
            val xAxis = chartBase.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            if (dataModel.size <= 10)
                xAxis.labelCount = dataModel.size
            xAxis.textColor = CompatUtil.getColorFromAttr(context, R.attr.titleColor)
            xAxis.valueFormatter = this
        }
    }

    class StepYAxisFormatter : ValueFormatter() {

        private lateinit var chartBase: BarLineChartBase<*>

        fun setChartBase(chartBase: BarLineChartBase<*>): StepYAxisFormatter {
            this.chartBase = chartBase
            return this
        }

        /**
         * Called when a value from an axis is to be formatted
         * before being drawn. For performance reasons, avoid excessive calculations
         * and memory allocations inside this method.
         *
         * @param value the value to be formatted
         * @param axis  the axis the value belongs to
         * @return formatted label value
         */
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val formatted = value.toInt()
            return formatted.toString()
        }

        fun build(context: Context) {
            chartBase.legend.textColor = CompatUtil.getColorFromAttr(context, R.attr.titleColor)

            val leftAxis = chartBase.axisLeft
            leftAxis.setLabelCount(5, false)
            leftAxis.spaceTop = 15f
            leftAxis.axisMinimum = 0f
            leftAxis.disableGridDashedLine()
            leftAxis.disableAxisLineDashedLine()
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            leftAxis.textColor = CompatUtil.getColorFromAttr(context, R.attr.titleColor)
            leftAxis.valueFormatter = this

            chartBase.axisRight.isEnabled = false
        }
    }
}
