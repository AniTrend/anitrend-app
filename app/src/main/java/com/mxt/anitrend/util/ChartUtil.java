package com.mxt.anitrend.util;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

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
            return String.valueOf(dataModel.get((int) value));
        }

        public void build() {
            XAxis xAxis = chartBase.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1);
            if(dataModel.size() <= 10)
                xAxis.setLabelCount(dataModel.size());
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
            return String.valueOf(value);
        }

        public void build() {
            YAxis[] yAxes = new YAxis[] { chartBase.getAxisLeft(), chartBase.getAxisRight() };
            for (YAxis yAxis: yAxes) {
                yAxis.setLabelCount(5, false);
                yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
                yAxis.setSpaceTop(15f);
                yAxis.setAxisMinimum(0f);
                yAxis.setValueFormatter(this);
            }
        }
    }
}
