package com.example.lukas.pooltemp.Controller;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.example.lukas.pooltemp.Activitys.MainActivity;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.R;
import com.example.lukas.pooltemp.Settings.Settings;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wicki on 07.04.2017.
 */
public class MPChartController {

    private MainActivity activity;
    private LineChart chart;
    Settings settings = Settings.getInstance();

    public MPChartController(MainActivity activity, LineChart chart) {
        this.activity = activity;
        this.chart = chart;
    }

    private void init() {

    }

    public void setData(List<Temperature> temperatureList) {

        if (temperatureList.isEmpty())
            return;

        LineData lineData = new LineData();

        ArrayList<Entry> values = new ArrayList<>();

        for (Temperature temp :
                temperatureList) {
            values.add(new Entry(temp.getTime().getTime(), (float) temp.getTemp()));
        }

        LineDataSet lineDataSet = configureLineDataSet(values);
        boolean withinOneDay=true;
        if(temperatureList.size()>1)
        {
            long diff=temperatureList.get(temperatureList.size()-1).getTime().getTime()-temperatureList.get(0).getTime().getTime();
            System.out.println("########################################"+temperatureList.get(0).getTime().toString());
            withinOneDay=TimeUnit.MILLISECONDS.toHours(diff)<=24;
        }
        configureAxis(!withinOneDay);

        lineData.addDataSet(lineDataSet);

        chart.getDescription().setEnabled(false);
        chart.setData(lineData);
        chart.setScaleYEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateX(settings.getPoolSettings().getAnimationDuration());
    }

    private void configureAxis(boolean dayFormat) {
        configureYAxis();
        configureXAxis(chart,dayFormat);
    }

    private void configureYAxis() {
        chart.getAxisLeft().setEnabled(settings.getPoolSettings().isyAxisEnabled());
        chart.getAxisRight().setEnabled(false);
    }

    private void configureXAxis(LineChart chart, boolean dayFormat) {
        XAxis xAxis = chart.getXAxis();

        xAxis.setEnabled(settings.getPoolSettings().isxAxisEnabled());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAvoidFirstLastClipping(true);
//        xAxis.setLabelRotationAngle(45);
//        xAxis.setSpaceMax(5);
//        xAxis.setXOffset(100);
        xAxis.setLabelCount(5, true);
        IAxisValueFormatter xAxisDayFormat = new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd.MM.yyyy");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mFormat.format(new Date((long) value));
            }
        };

        IAxisValueFormatter xAxisHourFormat = new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mFormat.format(new Date((long) value));
            }
        };

        if (dayFormat)
            xAxis.setValueFormatter(xAxisDayFormat);
        else
            xAxis.setValueFormatter(xAxisHourFormat);
    }

    @NonNull
    private LineDataSet configureLineDataSet(ArrayList<Entry> values) {
        LineDataSet lineDataSet = new LineDataSet(values, "Temperature");
        lineDataSet.setFillColor(Color.parseColor("#758cbb"));
        lineDataSet.setDrawFilled(true);
        lineDataSet.setColor(Color.parseColor("#303F9F"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleColor(Color.parseColor("#303F9F"));
        lineDataSet.setValueTextSize(0f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setHighlightEnabled(true);

        if (settings.getPoolSettings().isCubicCurves())
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        else
            lineDataSet.setMode(LineDataSet.Mode.LINEAR);
//        lineDataSet.setCubicIntensity(1f);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);


        return lineDataSet;
    }

    public void zoomOutVertical() {
        chart.zoomToCenter(1, 1 - settings.getPoolSettings().getZoomingMultiplier());
        chart.postInvalidate();
    }

    public void zoomVertical() {
        chart.zoomToCenter(1, 1 + settings.getPoolSettings().getZoomingMultiplier());
        chart.postInvalidate();

    }
}
