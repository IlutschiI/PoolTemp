package com.example.lukas.pooltemp;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.view.Tooltip;
import com.example.lukas.pooltemp.Adapter.EndDateSpinnerAdapter;
import com.example.lukas.pooltemp.Adapter.LockableScrollView;
import com.example.lukas.pooltemp.Adapter.StartDateSpinnerAdapter;
import com.example.lukas.pooltemp.Controller.HelloChartController;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.RESTController.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PointValue;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    float[] dataFloat;
    Tooltip tip;
    FloatingActionButton fab;
    LockableScrollView scrollView;
    Spinner startDateSpinner;
    Spinner endDateSpinner;
    TemperatureDataSource TempSource;
    SimpleDateFormat sdf;
    boolean startDateSpinnerInitialized = false;
    boolean endDateSpinnerInitialized = false;
    boolean isScrollable = true;
    StartDateSpinnerAdapter startDateSpinnerAdapter;
    EndDateSpinnerAdapter endDateSpinnerAdapter;
    MainActivity instance;
    TextView tvHighestTemp;
    TextView tvLowestTemp;
    TextView tvAccTemp;
    CardView ttSeekbar;
    SeekBar sbTime;
    CardView ttSeekbarEnd;
    SeekBar sbTimeEnd;
    ProgressDialog progressBar;
    TextView tvYesterdayTemp;
    lecho.lib.hellocharts.view.LineChartView helloChart;
    HelloChartController helloController;
    Date minDate;
    Date maxDate;

    List<Temperature> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        TempSource = TemperatureDataSource.getInstance(this);
        if (TempSource.countEntries() != 0) {
            RestController.getTempsSince(instance, TempSource.getActualTemperature().getTime());
        } else
            RestController.getAllTemps(instance);
        initControls();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        TextView tv = (TextView) findViewById(R.id.mainTV);

        //Init.initDB(TempSource);


        //LinechartController
        //controller = new LineChartController(this,(LineChartView)findViewById(R.id.linechart));
        //controller.initChart();

        progressBar.show();


        helloController = new HelloChartController(this, helloChart);
        helloController.setData(TempSource.getAllTemperatures());


/*
        List<Temperature> temps=TempSource.getAllTemperatures();
        dataFloat= new float[temps.size()];
        for (int i = 0; i<temps.size();i++) {
            tv.setText(tv.getText() +"\n "+temps.get(i).toString());
            dataFloat[i]=(float)temps.get(i).getTemp();
        }
        tv.setText(tv.getText() + "\n \nEs sind " + TempSource.countEntries() + " Einträge vorhanden");


        LineSet line = new LineSet(new String[] {"10","20","30","40","50","","","","","",""},dataFloat);

        line.setFill(Color.parseColor("#311B92"))
                .setSmooth(true)
                .setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.CYAN);
        //controller.addLine(line);
        //controller.showChart();
*/
        //Scrollview
        //Der FAB wird, wenn hinutergescrolled wird, ausgeblendet


        List<String> dateList = new LinkedList<>();
        dateList.add("Datum auswählen");
        for (Date d :
                TempSource.getAllPossibleDates()) {


            int yearInResult = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", d));
            int monthInResult = Integer.valueOf((String) android.text.format.DateFormat.format("MM", d));
            int dayInMonth = Integer.valueOf((String) android.text.format.DateFormat.format("dd", d));

            dateList.add("" + dayInMonth + "." + monthInResult + "." + yearInResult);

        }
        ArrayAdapter<String> spinneradapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dateList);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startDateSpinnerAdapter = new StartDateSpinnerAdapter(this, TempSource.getAllPossibleDates());
        endDateSpinnerAdapter = new EndDateSpinnerAdapter(this, TempSource.getAllPossibleDates());


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    public void updateHelloChart() {
        setInfoCardText();
        helloController.setStartEndOfData(minDate, maxDate);
        progressBar.dismiss();
        setFabEnabled(true);
    }

    /*
        Fab wird Enabled bzw. Disabled und die Farbe wird dementsprechend geändert
     */
    public void setFabEnabled(boolean b) {
        Drawable background = fab.getBackground();
        int alpha = background.getAlpha();
        fab.setEnabled(b);
        if (b) {


            //fab.setBackgroundColor(Color.parseColor("#FF4081"));
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));

        } else
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#93264B")));

    }

    public void setInfoCardText() {
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

    public void initSeekBars() {
        ttSeekbar = (CardView) findViewById(R.id.ttSeekbar);
        sbTime = (SeekBar) findViewById(R.id.sbTime);

        sbTime.setMax(TempSource.getDateRange());
        sbTime.incrementProgressBy(1);
        sbTime.setProgress(0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TempSource.getAllPossibleDates().get(0));
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
                calendar.setTime(TempSource.getAllPossibleDates().get(0));
                calendar.add(Calendar.DATE, i);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.MINUTE, 0);
                minDate = calendar.getTime();
                int x = seekBar.getThumb().getBounds().left;
                ((TextView) ttSeekbar.findViewById(R.id.ttSeekbarValue)).setText(sdf.format(calendar.getTime()));
                ttSeekbar.setX(x);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ttSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ttSeekbar.setVisibility(View.INVISIBLE);
                updateHelloChart();
                //updateChartsRange();
            }
        });

        ttSeekbarEnd = (CardView) findViewById(R.id.ttSeekbarEnd);

        calendar.setTime(TempSource.getAllPossibleDates().get(TempSource.getAllPossibleDates().size() - 1));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.MINUTE, 59);
        maxDate = calendar.getTime();
        ((TextView) ttSeekbarEnd.findViewById(R.id.ttSeekbarEndValue)).setText(sdf.format(calendar.getTime()));
        sbTimeEnd = (SeekBar) findViewById(R.id.sbTimeEnd);
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
                calendar.setTime(TempSource.getAllPossibleDates().get(0));
                calendar.add(Calendar.DATE, i);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.MINUTE, 59);
                maxDate = calendar.getTime();
                ((TextView) ttSeekbarEnd.findViewById(R.id.ttSeekbarEndValue)).setText(sdf.format(calendar.getTime()));

                // Toast.makeText(getBaseContext(),"not allowed",Toast.LENGTH_LONG).show();


                int x = seekBar.getThumb().getBounds().left;
                //((TextView)ttSeekbarEnd.findViewById(R.id.ttSeekbarEndValue)).setText(i+":59");
                ttSeekbarEnd.setX(x);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ttSeekbarEnd.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ttSeekbarEnd.setVisibility(View.INVISIBLE);
                //updateChartsRange();
                updateHelloChart();
            }
        });

    }

    private void initControls() {

        sdf = new SimpleDateFormat("dd.MM.yyyy");
        tvHighestTemp = (TextView) findViewById(R.id.tvHighestTemp);
        tvLowestTemp = (TextView) findViewById(R.id.tvLowestTemp);
        tvAccTemp = (TextView) findViewById(R.id.tvAccTemp);
        tvYesterdayTemp = (TextView) findViewById(R.id.tvYesterdayTemp);

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Daten werden aktualisiert ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(-1);

        helloChart = (lecho.lib.hellocharts.view.LineChartView) findViewById(R.id.helloLinechart);
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


        scrollView = ((LockableScrollView) findViewById(R.id.scrollview));
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int i = scrollView.getScrollY();

                if (i >= 130) {
                    fab.hide();
                } else
                    fab.show();
            }
        });


        fab = (FloatingActionButton) findViewById(R.id.fab);    //FloatingActionButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                      //Onclicklistener des FloatingActionButtons
                //if (!controller.isanimating())
                //   controller.addPoint(new Point("", 25));
                fab.setEnabled(false);
                progressBar.show();
                RestController.getTempsSince(instance, TempSource.getActualTemperature().getTime());

            }
        });

        initSeekBars();
    }

    public void setSelectedPointCardText(Temperature acc) {

        if (acc == null) {
            ((TextView) findViewById(R.id.tvSelctedTemp)).setText("N/A");
            ((TextView) findViewById(R.id.tvSelctedTempTime)).setText("N/A");
            return;
        }

        ((TextView) findViewById(R.id.tvSelctedTemp)).setText(round(acc.getTemp(), 2) + "°C");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        ((TextView) findViewById(R.id.tvSelctedTempTime)).setText(simpleDateFormat.format(acc.getTime()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload) {
            progressBar.show();
            RestController.getAllTemps(instance);
            return true;
        }

        if (id == R.id.action_forceTemperature) {
            progressBar.show();
            RestController.forceNewTemperature(instance);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
