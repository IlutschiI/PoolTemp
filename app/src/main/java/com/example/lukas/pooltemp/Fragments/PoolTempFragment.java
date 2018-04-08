package com.example.lukas.pooltemp.Fragments;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.lukas.pooltemp.Activitys.MainActivity;
import com.example.lukas.pooltemp.Adapter.LockableScrollView;
import com.example.lukas.pooltemp.Controller.HelloChartController;
import com.example.lukas.pooltemp.Controller.MPChartController;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.R;
import com.example.lukas.pooltemp.RESTController.RestController;
import com.example.lukas.pooltemp.RESTController.TemperatureRestController;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by wicki on 18.11.2016.
 */
public class PoolTempFragment extends Fragment {

    HelloChartController helloController;
    MainActivity activity;
    lecho.lib.hellocharts.view.LineChartView helloChart;
    LineChart mpChart;
    MPChartController mpChartController;
    TemperatureDataSource tempSource;
    boolean initSeekbars = false;
    List<Date> possibleDates;
    Date minDate;
    Date maxDate;
    CardView ttSeekbar;
    TextView tvHighestTemp;
    TextView tvLowestTemp;
    TextView tvAccTemp;
    TextView tvAmountOfData;
    SeekBar sbTimeStart;
    CardView ttSeekbarEnd;
    SeekBar sbTimeEnd;
    TextView tvYesterdayTemp;
    ImageButton ib_zoom;
    ImageButton ib_zoomOut;
    SimpleDateFormat sdf;
    LockableScrollView scrollView;
    boolean isScrollable = true;
    FloatingActionButton fab;
    PoolTempFragment instance;
    ProgressDialog pdLoad;

    Spinner spSensor;

    List<Temperature> temps;
    List<String> sensors=new ArrayList<>();

    String selectedSensor="";

    View view;
    TemperatureRestController temperatureRestController;


    public PoolTempFragment() {
        activity = MainActivity.instance;
        instance = this;
        temperatureRestController = new TemperatureRestController(activity.getApplicationContext());
        if (activity != null)
            tempSource = TemperatureDataSource.getInstance(activity);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (activity == null) {
            activity = MainActivity.instance;
            tempSource = TemperatureDataSource.getInstance(activity);
        }
        view = inflater.inflate(R.layout.content_main, container, false);

        possibleDates = tempSource.getAllPossibleDates();
        if (!possibleDates.isEmpty()) {
            minDate = possibleDates.get(0);
            maxDate = possibleDates.get(possibleDates.size() - 1);
        } else {
            minDate = new Date(0);
            maxDate = new Date(Long.MAX_VALUE);
        }
//        initSeekBars();
        initControls();


        //progressDialog.dismiss();


        fab = (FloatingActionButton) view.findViewById(R.id.fab);    //FloatingActionButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                      //Onclicklistener des FloatingActionButtons
                activity.updateProgress(-1, 0);
                Date lastDate = tempSource.getActualTemperature().getTime();
                temperatureRestController.getTempsSince(lastDate, new Response.Listener<Temperature[]>() {
                    @Override
                    public void onResponse(Temperature[] response) {
                        tempSource.insertTemperatureMany(Arrays.asList(response), activity);
                        activity.resetProgress();
                        setInfoCardText();
                        updateMPChart();
                        refreshPossibleDates();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(activity, "somewthing went wrong", Toast.LENGTH_SHORT).show();
                        activity.resetProgress();
                    }
                });

            }
        });

        setInfoCardText();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        System.out.println("################################ On Resume #########################");
        //tempSource=TemperatureDataSource.getInstance(activity);
        if (possibleDates.size() != 0)
            initSeekBars();
    }

    public void updateMPChart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setInfoCardText();
                possibleDates = tempSource.getAllPossibleDates();
                if (!initSeekbars && possibleDates.size() != 0) {
                    initSeekBars();
                }

                mpChartController.setData(getTempsBetweenDateForSensor());
                //progressDialog.dismiss();

                pdLoad.dismiss();
                System.out.println("----------------------------Chart Updated--------------------------");
            }
        });
    }


    public void initSeekBars() {
        initSeekbars = true;
        ttSeekbar = (CardView) view.findViewById(R.id.ttSeekbar);
        sbTimeStart = (SeekBar) view.findViewById(R.id.sbTime);

        sbTimeStart.setMax(tempSource.getDateRange());
        sbTimeStart.incrementProgressBy(1);
        sbTimeStart.setProgress(0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(possibleDates.get(0));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MINUTE, 0);
        minDate = calendar.getTime();
        ((TextView) ttSeekbar.findViewById(R.id.ttSeekbarValue)).setText(sdf.format(calendar.getTime()));

        sbTimeStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN)
                    ttSeekbar.setVisibility(View.VISIBLE);
                else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL)
                    ttSeekbar.setVisibility(View.INVISIBLE);


                return false;
            }
        });
        sbTimeStart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //preventing from overlapping the 2 seekbars
                if (i > sbTimeEnd.getProgress()) {
                    seekBar.setProgress(i - 1);
                    return;
                    // Toast.makeText(getBaseContext(),"not allowed",Toast.LENGTH_LONG).show();
                }
                //Calc MinDate
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    int x = seekBar.getThumb().getBounds().left;
                    if (((double) x / width) >= 0.9)
                        x = (int) (width * 0.90);
                    ttSeekbar.setX(x);
                } else {

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateMPChart();
                    }
                }).start();
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
        sbTimeEnd.setMax(tempSource.getDateRange());
        sbTimeEnd.incrementProgressBy(1);
        sbTimeEnd.setProgress(tempSource.getDateRange());
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
                if (i < sbTimeStart.getProgress()) {
                    seekBar.setProgress(i + 1);
                    return;
                    // TODO do the same as done in sbTimeStart
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    int x = seekBar.getThumb().getBounds().left;
                    if (((double) x / width) >= 0.9)
                        x = (int) (width * 0.90);
                    ttSeekbarEnd.setX(x);
                } else {

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
                ttSeekbar.setVisibility(View.INVISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updateMPChart();
                    }
                }).start();
            }
        });

    }

    private void initControls() {


        sdf = new SimpleDateFormat("dd.MM.yyyy");
        tvHighestTemp = (TextView) view.findViewById(R.id.tvHighestTemp);
        tvLowestTemp = (TextView) view.findViewById(R.id.tvLowestTemp);
        tvAccTemp = (TextView) view.findViewById(R.id.tvAccTemp);
        tvYesterdayTemp = (TextView) view.findViewById(R.id.tvYesterdayTemp);
        tvAmountOfData = (TextView) view.findViewById(R.id.tvAmountOfData);


        if (pdLoad == null)
            pdLoad = new ProgressDialog(activity);
        pdLoad.setIndeterminate(true);
        pdLoad.setTitle("Bitte Warten");
        pdLoad.setMessage("Daten werden geladen...");
        pdLoad.show();


//        initalizeHelloChart();
        initializeSensorSpinner();
        initializeMPChart();


        scrollView = ((LockableScrollView) view.findViewById(R.id.scrollview));
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int i = scrollView.getScrollY();

                if (i >= 130) {
                    //activity.showOrHideFab(false);
                    fab.hide();
                } else
                    //activity.showOrHideFab(true);
                    fab.show();

            }
        });

        ib_zoom = (ImageButton) view.findViewById(R.id.ib_zoom);
        ib_zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpChartController.zoomVertical();
            }
        });

        ib_zoomOut = (ImageButton) view.findViewById(R.id.ib_zoom_out);
        ib_zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpChartController.zoomOutVertical();
            }
        });
    }

    private void initializeSensorSpinner() {
        spSensor=(Spinner) view.findViewById(R.id.spSensor);
        for (Temperature temperature :
                tempSource.getAllTemperatures()) {
            if(!sensors.contains(temperature.getSensorID())){
                sensors.add(temperature.getSensorID());
            }
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_spinner_item,sensors);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSensor.setAdapter(adapter);

        spSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               selectedSensor= (String)adapterView.getItemAtPosition(i);
               updateMPChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedSensor="";
                updateMPChart();
            }
        });
    }

    private void initializeMPChart() {
        mpChart = (LineChart) view.findViewById(R.id.mpLinechart);
        mpChart.setOnTouchListener(new View.OnTouchListener() {
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

        mpChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Temperature accTemp = new Temperature(e.getY(), new Date((long) e.getX()));
                setSelectedPointCardText(accTemp);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        mpChartController = new MPChartController(activity, mpChart);

        //mpChartController.setData(tempSource.getAllTemperatures());

        mpChartController.setData(getTempsBetweenDateForSensor());
        pdLoad.dismiss();

    }

    private List<Temperature> getTempsBetweenDateForSensor() {
        List<Temperature> temperatureList = tempSource.getTempsBetween(minDate, maxDate,selectedSensor);
        return temperatureList;
    }

    private void initalizeHelloChart() {
        helloChart = (lecho.lib.hellocharts.view.LineChartView) view.findViewById(R.id.helloLinechart);
        helloChart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
                Temperature accTemp = new Temperature(value.getY(), new Date((long) value.getX()));
                setSelectedPointCardText(accTemp);
            }

            @Override
            public void onValueDeselected() {
                setSelectedPointCardText(null);
            }
        });

        helloController = new HelloChartController(activity, helloChart);
        if (temps == null || temps.size() == 0) {
            temps = tempSource.getAllTemperatures();
            for (Temperature temperature :
                    temps) {
                if(sensors.contains(temperature.getSensorID())){
                    sensors.add(temperature.getSensorID());
                }
            }
        }
        helloController.setData(temps);
        updateMPChart();


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
    }

    /*
    Fab wird Enabled bzw. Disabled und die Farbe wird dementsprechend geändert
 */
    public void setInfoCardText() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    tvHighestTemp.setText("" + tempSource.getHighestTemperature().getTemperature() + "°C");
                } catch (Exception e) {
                    tvHighestTemp.setText("N/A");
                }

                try {
                    tvLowestTemp.setText("" + tempSource.getLowestTemperature().getTemperature() + "°C");
                } catch (Exception e) {
                    tvLowestTemp.setText("N/A");
                }

                try {
                    tvAccTemp.setText("" + tempSource.getActualTemperature().getTemperature() + "°C");
                } catch (Exception e) {
                    tvAccTemp.setText("N/A");
                }
                try {
                    tvYesterdayTemp.setText("" + tempSource.getAverageOfYesterday() + "°C");
                } catch (Exception e) {
                    tvYesterdayTemp.setText("N/A");
                }
                try {
                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
                    DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

                    symbols.setGroupingSeparator(' ');
                    formatter.setDecimalFormatSymbols(symbols);

                    tvAmountOfData.setText("" + formatter.format(tempSource.countEntries()));
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

        ((TextView) view.findViewById(R.id.tvSelctedTemp)).setText(round(acc.getTemperature(), 1) + "°C");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        ((TextView) view.findViewById(R.id.tvSelctedTempTime)).setText(simpleDateFormat.format(acc.getTime()));
    }

    public void runOnUiThread(Runnable r) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(r);
    }

    public void refreshPossibleDates() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                possibleDates = tempSource.getAllPossibleDates();
                if (!possibleDates.isEmpty()) {
                    initSeekBars();
                }
            }
        });
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
