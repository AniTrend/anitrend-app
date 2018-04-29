package com.mxt.anitrend.util;

import android.content.Context;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.mxt.anitrend.R;

import java.util.List;

/**
 * Created by max on 2018/02/24.
 */

public class ChartUtil {

    public static class StepXAxisFormatter<K> implements IAxisValueFormatter {

        private List<K> dataModel;
        private BarLineChartBase<?> chartBase;

        public StepXAxisFormatter setChartBase(BarLineChartBase<?> chartBase) {
            this.chartBase = chartBase;
            return this;
        }

        public StepXAxisFormatter<K> setDataModel(List<K> dataModel) {
            this.dataModel = dataModel;
            return this;
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
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int index = (int) value;
            K model = dataModel.get(index);
            return String.valueOf(model);
        }

        public void build(Context context) {
            XAxis xAxis = chartBase.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1);
            if(dataModel.size() <= 10)
                xAxis.setLabelCount(dataModel.size());
            xAxis.setTextColor(CompatUtil.getColorFromAttr(context, R.attr.titleColor));
            xAxis.setValueFormatter(this);
        }
    }

    public static class StepYAxisFormatter implements IAxisValueFormatter {

        private BarLineChartBase<?> chartBase;

        public StepYAxisFormatter setChartBase(BarLineChartBase<?> chartBase) {
            this.chartBase = chartBase;
            return this;
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
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int formatted = (int) value;
            return String.valueOf(formatted);
        }

        public void build(Context context) {
            chartBase.getLegend().setTextColor(CompatUtil.getColorFromAttr(context, R.attr.titleColor));

            YAxis leftAxis = chartBase.getAxisLeft();
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(15f);
            leftAxis.setAxisMinimum(0f);
            leftAxis.disableGridDashedLine();
            leftAxis.disableAxisLineDashedLine();
            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            leftAxis.setTextColor(CompatUtil.getColorFromAttr(context, R.attr.titleColor));
            leftAxis.setValueFormatter(this);

            chartBase.getAxisRight().setEnabled(false);
        }
    }
}
