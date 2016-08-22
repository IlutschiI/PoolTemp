package com.example.lukas.pooltemp.Controller;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.SineEase;
import com.example.lukas.pooltemp.MainActivity;
import com.example.lukas.pooltemp.R;

/**
 * Created by Lukas on 19.08.2016.
 */
public class LineChartController {

    private MainActivity c;
    private LineChartView chart;
    private Tooltip tip;
    boolean isanimating=false;

    public LineChartController(MainActivity c, LineChartView chart){
        this.c=c;
        this.chart=chart;
    }

    public void initChart(){
        initTooltip();
        chart.setAxisBorderValues(0, 40, 10);
        chart.setXAxis(true);
        chart.setBorderSpacing(Tools.fromDpToPx(15));
        chart.setYAxis(true);
        chart.setXLabels(AxisController.LabelPosition.OUTSIDE);



        chart.setTooltips(tip);
    }

    public void showChart(){
        Animation anim = new Animation()
                .setEasing(new com.db.chart.view.animation.easing.SineEase());
        isanimating=true;
        c.setFabEnabled(!isanimating());
        chart.show(anim.setEndAction(new Runnable() {
            @Override
            public void run() {
                isanimating=false;

                c.setFabEnabled(!isanimating());
            }
        }));
    }
    public void showChart(final Runnable r){
        Animation anim = new Animation()
                .setEasing(new com.db.chart.view.animation.easing.SineEase());
        isanimating=true;
        c.setFabEnabled(!isanimating());
        chart.show(anim.setEndAction(new Runnable() {
            @Override
            public void run() {
                isanimating=false;

                c.setFabEnabled(!isanimating());
                r.run();
            }
        }));
    }

    public void initTooltip(){
        tip=new Tooltip(c, R.layout.linechart_tooltip,R.id.tooltipValue);
        tip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

        tip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

        tip.setPivotX(Tools.fromDpToPx(65) / 2);
        tip.setPivotY(Tools.fromDpToPx(25));
        tip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        tip.setDimensions((int) Tools.fromDpToPx(65), (int) Tools.fromDpToPx(25));

    }

    public void addLine(final LineSet line){

            chart.reset();
            chart.addData(line);
            showChart();


    }

    public void addPoint(final Point point){
        final LineSet line=((LineSet) chart.getData().get(0));

        isanimating=true;

        c.setFabEnabled(!isanimating());
        chart.dismissAllTooltips();
        chart.dismiss(new Animation().setEasing(new SineEase()).setEndAction(new Runnable() {
            @Override
            public void run() {

                doInMainThread(new Runnable() {
                    @Override
                    public void run() {

                        line.addPoint(point);
                        line.setDotsColor(Color.CYAN);

                        chart.reset();
                        initChart();

                        addLine(line);
                        chart.setAxisBorderValues(0, 40, 10);
                        showChart();
                    }
                });
                //chart.reset();
                //initChart();
                //addLine(line);

            }
        }));



       /* LineSet line=((LineSet) chart.getData().get(0));
        line.addPoint(point);
        float[] values=new float[line.size()];

        for (int i = 0;i<line.size();i++){
            values[i]=line.getValue(i);
        }

        chart.updateValues(0,values);
        chart.notifyDataUpdate();*/
/*
        if(chart.isShown())
            chart.dismiss(new Animation().setEasing(new SineEase()).setEndAction(new Runnable() {
                @Override
                public void run() {
                    chart.cle
                    doInMainThread(new Runnable() {
                        @Override
                        public void run() {
                            showChart();
                        }
                    });
                }
            }));
        else {
            ((LineSet)chart.getData().get(0)).addPoint(point);
            showChart();
        }*/

    }

    public void doInMainThread(Runnable r){
        Handler handler = new Handler(c.getMainLooper());
        handler.post(r);
    }
    public void dismissChart(Runnable r){
        chart.dismiss(new Animation().setEasing(new SineEase()).setEndAction(r));
    }

    public boolean isanimating() {
        return isanimating;
    }
}
