package com.example.lukas.pooltemp.Fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.lukas.pooltemp.Activitys.MainActivity;
import com.example.lukas.pooltemp.Adapter.LockableScrollView;
import com.example.lukas.pooltemp.Controller.HelloChartController;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.R;
import com.example.lukas.pooltemp.RESTController.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by wicki on 18.11.2016.
 */
public class PoolTempFragment extends Fragment {

    HelloChartController helloController;
    MainActivity activity;
    lecho.lib.hellocharts.view.LineChartView helloChart;
    TemperatureDataSource TempSource;
    boolean initSeekbars = false;
    List<Date> possibleDates;
    Date minDate;
    Date maxDate;
    CardView ttSeekbar;
    TextView tvHighestTemp;
    TextView tvLowestTemp;
    TextView tvAccTemp;
    SeekBar sbTime;
    CardView ttSeekbarEnd;
    SeekBar sbTimeEnd;
    TextView tvYesterdayTemp;
    ImageButton ib_zoom;
    ImageButton ib_zoomOut;
    boolean fabEnabled=true;
    SimpleDateFormat sdf;
    LockableScrollView scrollView;
    boolean isScrollable = true;
    FloatingActionButton fab;
    PoolTempFragment instance;

    View view;


    public PoolTempFragment() {
        activity=MainActivity.instance;
        instance=this;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.content_main,container,false);


        new Thread(new Runnable() {
            @Override
            public void run() {
                initControls();

                helloController = new HelloChartController(activity, helloChart);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        helloController.setData(TempSource.getAllTemperatures());
                    }
                });

                updateHelloChart();
                //progressDialog.dismiss();
            }
        }).start();

        return view;
    }

    public void updateHelloChart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setInfoCardText();
                if (!initSeekbars)
                    initSeekBars();
                possibleDates = TempSource.getAllPossibleDates();
                helloController.setStartEndOfData(minDate, maxDate);
                //progressDialog.dismiss();
                setFabEnabled(true);
                System.out.println("----------------------------System running--------------------------");
            }
        });

    }

    public void initSeekBars() {
        initSeekbars = true;
        ttSeekbar = (CardView) view.findViewById(R.id.ttSeekbar);
        sbTime = (SeekBar) view.findViewById(R.id.sbTime);

        sbTime.setMax(TempSource.getDateRange());
        sbTime.incrementProgressBy(1);
        sbTime.setProgress(0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(possibleDates.get(0));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MINUTE, 0);
        minDate = calendar.getTime();
        ((TextView) ttSeekbar.findViewById(R.id.ttSeekbarValue)).setText(sdf.format(calendar.getTime()));

        sbTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN)
                    ttSeekbar.setVisibility(View.VISIBLE);
                else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL)
                    ttSeekbar.setVisibility(View.INVISIBLE);


                return false;
            }
        });
        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i > sbTimeEnd.getProgress()) {
                    seekBar.setProgress(i - 1);
                    return;
                    // Toast.makeText(getBaseContext(),"not allowed",Toast.LENGTH_LONG).show();
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(possibleDates.get(0));
                calendar.add(Calendar.DATE, i);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.MINUTE, 0);
                minDate = calendar.getTime();


                ((TextView) ttSeekbar.findViewById(R.id.ttSeekbarValue)).setText(sdf.format(calendar.getTime()));
                int width = seekBar.getWidth()
                        - seekBar.getPaddingLeft()
                        - seekBar.getPaddingRight();
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                    int x = seekBar.getThumb().getBounds().left;
                    if(((double)x/width)>=0.9)
                        x=(int)(width*0.90);
                    ttSeekbar.setX(x);
                }
                else
                {

                    int thumbPos = seekBar.getPaddingLeft()
                            + width
                            * seekBar.getProgress()
                            / seekBar.getMax();

                    if (thumbPos > width - 20) {
                        thumbPos = width - 80;
                    }
                    CardView.LayoutParams layoutParams = new CardView.LayoutParams(
                            CardView.LayoutParams.WRAP_CONTENT, CardView.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(thumbPos - 20, 100, 0, 0);

                    ttSeekbar.setLayoutParams(layoutParams);
                    ttSeekbar.requestLayout();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ttSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ttSeekbar.setVisibility(View.INVISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //progressDialog.show();
                    }
                });



                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateHelloChart();
                    }
                }).start();
                //updateChartsRange();
            }
        });

        ttSeekbarEnd = (CardView) view.findViewById(R.id.ttSeekbarEnd);

        calendar.setTime(possibleDates.get(possibleDates.size() - 1));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.MINUTE, 59);
        maxDate = calendar.getTime();
        ((TextView) ttSeekbarEnd.findViewById(R.id.ttSeekbarEndValue)).setText(sdf.format(calendar.getTime()));
        sbTimeEnd = (SeekBar) view.findViewById(R.id.sbTimeEnd);
        sbTimeEnd.setMax(TempSource.getDateRange());
        sbTimeEnd.incrementProgressBy(1);
        sbTimeEnd.setProgress(TempSource.getDateRange());
        sbTimeEnd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN)
                    ttSeekbarEnd.setVisibility(View.VISIBLE);
                else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL)
                    ttSeekbarEnd.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        sbTimeEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i < sbTime.getProgress()) {
                    seekBar.setProgress(i + 1);
                    return;
                    // TODO do the same as done in sbTime
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(possibleDates.get(0));
                calendar.add(Calendar.DATE, i);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.MINUTE, 59);
                maxDate = calendar.getTime();
                ((TextView) ttSeekbarEnd.findViewById(R.id.ttSeekbarEndValue)).setText(sdf.format(calendar.getTime()));

                // Toast.makeText(getBaseContext(),"not allowed",Toast.LENGTH_LONG).show();


                int width = seekBar.getWidth()
                        - seekBar.getPaddingLeft()
                        - seekBar.getPaddingRight();
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                    int x = seekBar.getThumb().getBounds().left;
                    if(((double)x/width)>=0.9)
                        x=(int)(width*0.90);
                    ttSeekbarEnd.setX(x);
                }
                else
                {

                    int thumbPos = seekBar.getPaddingLeft()
                            + width
                            * seekBar.getProgress()
                            / seekBar.getMax();

                    if (thumbPos > width - 20) {
                        thumbPos = width - 80;
                    }
                    CardView.LayoutParams layoutParams = new CardView.LayoutParams(
                            CardView.LayoutParams.WRAP_CONTENT, CardView.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(thumbPos - 20, 100, 0, 0);

                    ttSeekbarEnd.setLayoutParams(layoutParams);
                    ttSeekbarEnd.requestLayout();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ttSeekbarEnd.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ttSeekbarEnd.setVisibility(View.INVISIBLE);
                //updateChartsRange();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //progressDialog.show();
                        updateHelloChart();
                    }
                });



            }
        });

    }

    private void initControls() {


        sdf = new SimpleDateFormat("dd.MM.yyyy");
        tvHighestTemp = (TextView) view.findViewById(R.id.tvHighestTemp);
        tvLowestTemp = (TextView) view.findViewById(R.id.tvLowestTemp);
        tvAccTemp = (TextView) view.findViewById(R.id.tvAccTemp);
        tvYesterdayTemp = (TextView) view.findViewById(R.id.tvYesterdayTemp);


        helloChart = (lecho.lib.hellocharts.view.LineChartView) view.findViewById(R.id.helloLinechart);
        helloChart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Temperature accTemp = new Temperature(value.getY(), new Date((long) value.getX()));
                setSelectedPointCardText(accTemp);
            }

            @Override
            public void onValueDeselected() {

            }
        });


        helloChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL || motionEvent.getAction() != MotionEvent.ACTION_UP)
                    isScrollable = false;
                else
                    isScrollable = true;
                scrollView.setScrollable(isScrollable);
                return false;
            }
        });


        scrollView = ((LockableScrollView) view.findViewById(R.id.scrollview));
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int i = scrollView.getScrollY();

                if (i >= 130) {
                    fab.hide();
                } else
                    fab.show();

                if(!fabEnabled)
                {
                    fab.hide();
                }
            }
        });


        fab = (FloatingActionButton) view.findViewById(R.id.fab);    //FloatingActionButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                      //Onclicklistener des FloatingActionButtons
                //if (!controller.isanimating())
                //   controller.addPoint(new Point("", 25));
                fab.setEnabled(false);
               //progressDialog.show();

                RestController.getTempsSince(activity, TempSource.getActualTemperature().getTime(),instance);

            }
        });


        ib_zoom=(ImageButton)view.findViewById(R.id.ib_zoom);
        ib_zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helloController.zoomVertical();
            }
        });

        ib_zoomOut=(ImageButton)view.findViewById(R.id.ib_zoom_out);
        ib_zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helloController.zoomOutVertical();
            }
        });

        //initSeekBars();
    }

    /*
    Fab wird Enabled bzw. Disabled und die Farbe wird dementsprechend geändert
 */
    public void setFabEnabled(final boolean b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.setEnabled(b);
                if (b) {


                    //fab.setBackgroundColor(Color.parseColor("#FF4081"));
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));

                } else
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#93264B")));
            }
        });
        //int alpha = background.getAlpha();


    }

    public void setInfoCardText() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    tvHighestTemp.setText("" + TempSource.getHighestTemperature().getTemp() + "°C");
                } catch (Exception e) {
                    tvHighestTemp.setText("N/A");
                }

                try {
                    tvLowestTemp.setText("" + TempSource.getLowestTemperature().getTemp() + "°C");
                } catch (Exception e) {
                    tvLowestTemp.setText("N/A");
                }

                try {
                    tvAccTemp.setText("" + TempSource.getActualTemperature().getTemp() + "°C");
                } catch (Exception e) {
                    tvAccTemp.setText("N/A");
                }
                try {
                    tvYesterdayTemp.setText("" + TempSource.getAverageOfYesterday() + "°C");
                } catch (Exception e) {
                    tvYesterdayTemp.setText("N/A");
                }
            }
        });


    }

    public void setSelectedPointCardText(Temperature acc) {

        if (acc == null) {
            ((TextView) view.findViewById(R.id.tvSelctedTemp)).setText("N/A");
            ((TextView) view.findViewById(R.id.tvSelctedTempTime)).setText("N/A");
            return;
        }

        ((TextView) view.findViewById(R.id.tvSelctedTemp)).setText(round(acc.getTemp(), 1) + "°C");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        ((TextView) view.findViewById(R.id.tvSelctedTempTime)).setText(simpleDateFormat.format(acc.getTime()));
    }

    public void runOnUiThread(Runnable r){
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(r);
    }

    public void refreshPossibleDates(){
        possibleDates = TempSource.getAllPossibleDates();
        initSeekBars();
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
