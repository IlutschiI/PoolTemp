package com.example.lukas.pooltemp.Controller;

import android.graphics.Color;

import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.MainActivity;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.formatter.SimpleAxisValueFormatter;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by wicki on 31.08.2016.
 */
public class HelloChartController {

    private MainActivity activity;
    private LineChartView chart;
    LineChartData lineChartData;

    public HelloChartController(MainActivity activity, LineChartView chart) {
        this.activity = activity;
        this.chart = chart;
        initChart();
    }

    public void initChart() {

        chart.setValueSelectionEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        System.out.println(chart.getMaxZoom()+"");
        chart.setMaxZoom(19);

    }

    public void setData(List<Temperature> tempList) {
        List<PointValue> values = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        int i = 0;
        for (Temperature t :
                tempList) {
            values.add(new PointValue(t.getTime().getTime(), (float) t.getTemp()));
            if (i % 5 == 0)
                axisValues.add(new AxisValue(t.getTime().getTime()).setLabel(sdf.format(t.getTime())));
            i++;

        }

        Line line = new Line(values);

        line.setColor(Color.parseColor("#758cbb"));
        line.setCubic(true);
        //line.setHasLabels(true);
        //line.setHasLabelsOnlyForSelected(true);

        line.setFilled(true);
        line.setHasPoints(true);
        line.setPointRadius(3);
        List<Line> lines = new ArrayList<>();
        lines.add(line);


        lineChartData = new LineChartData(lines);

        /*
        axisValues.add(new AxisValue(getFirstOfList(tempList).getTime().getTime())
                .setLabel(sdf.format(getFirstOfList(tempList).getTime())));
        axisValues.add(new AxisValue(getLastOfList(tempList).getTime().getTime())
                .setLabel(sdf.format(getLastOfList(tempList).getTime())));
                */

        Axis XAxis = new Axis(axisValues);
        XAxis.setHasLines(true);
        XAxis.setMaxLabelChars(10);
        XAxis.setAutoGenerated(false);

        Axis YAxis = Axis.generateAxisFromRange(-10, 40, 0.5f);
        YAxis.setMaxLabelChars(4);
        YAxis.setFormatter(new SimpleAxisValueFormatter(1));
        YAxis.setHasLines(true);

        lineChartData.setAxisXBottom(XAxis);
        lineChartData.setAxisYLeft(YAxis);


        chart.setLineChartData(lineChartData);
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = 30;
        //v.right*=1.000008;


        chart.setMaximumViewport(v);
        chart.setCurrentViewportWithAnimation(v,1000);


    }

    public void setStartEndOfData(Date startDate, Date endDate) {

        List<Temperature> tempList=TemperatureDataSource.getInstance(activity).getTemps(startDate,endDate);

        setData(tempList);

    }

    public Temperature getFirstOfList(List<Temperature> list) {
        if (list == null || list.size() == 0)
            return null;
        else
            return list.get(0);
    }

    public Temperature getLastOfList(List<Temperature> list) {
        if (list == null || list.size() == 0)
            return null;
        else
            return list.get(list.size() - 1);
    }
}
