package com.example.lukas.pooltemp.Activitys;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.example.lukas.pooltemp.R;
import com.example.lukas.pooltemp.RESTController.RestController;
import com.example.lukas.pooltemp.Settings.Settings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    boolean initSeekbars = false;
    boolean fabEnabled=true;
    boolean poolScreen=true;
    List<Date> possibleDates;
    ImageButton ib_zoom;
    ImageButton ib_zoomOut;
    LinearLayout contentPanel;
    SettingsActivity settingsActivity;

    List<Temperature> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("---------------------------------------System start----------------------------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentPanel=(LinearLayout) findViewById(R.id.contentPanel);
        contentPanel.addView(getLayoutInflater().inflate(R.layout.content_main,null));

        instance = this;

        settingsActivity=new SettingsActivity(instance);
        settingsActivity.updateSettings();
        if(Settings.getInstance().getPoolSettings().getNumberOfPoints()==0)
            Settings.getInstance().getPoolSettings().setNumberOfPoints(100);


        TempSource = TemperatureDataSource.getInstance(this);
        if (TempSource.countEntries() != 0) {
            //RestController.getTempsSince(instance, TempSource.getActualTemperature().getTime());
        } else {
            RestController.getAllTemps(instance);
            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!before all Dates");
        possibleDates = TempSource.getAllPossibleDates();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!after all Dates");

        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Daten werden aktualisiert ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(-1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.show();
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                initControls();

                helloController = new HelloChartController(instance, helloChart);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        helloController.setData(TempSource.getAllTemperatures());
                    }
                });

                updateHelloChart();
                progressBar.dismiss();
            }
        }).start();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //TextView tv = (TextView) findViewById(R.id.mainTV);

        //Init.initDB(TempSource);


        //LinechartController
        //controller = new LineChartController(this,(LineChartView)findViewById(R.id.linechart));
        //controller.initChart();







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
                possibleDates) {


            int yearInResult = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", d));
            int monthInResult = Integer.valueOf((String) android.text.format.DateFormat.format("MM", d));
            int dayInMonth = Integer.valueOf((String) android.text.format.DateFormat.format("dd", d));

            dateList.add("" + dayInMonth + "." + monthInResult + "." + yearInResult);

        }




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                progressBar.dismiss();
                setFabEnabled(true);
                System.out.println("----------------------------System running--------------------------");
            }
        });

    }

    /*
        Fab wird Enabled bzw. Disabled und die Farbe wird dementsprechend geändert
     */
    public void setFabEnabled(final boolean b) {
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {
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

        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {
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

    public void initSeekBars() {
        initSeekbars = true;
        ttSeekbar = (CardView) findViewById(R.id.ttSeekbar);
        sbTime = (SeekBar) findViewById(R.id.sbTime);

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
                        progressBar.show();
                    }
                });



                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //progressBar.show();
                        updateHelloChart();
                    }
                }).start();
                //updateChartsRange();
            }
        });

        ttSeekbarEnd = (CardView) findViewById(R.id.ttSeekbarEnd);

        calendar.setTime(possibleDates.get(possibleDates.size() - 1));
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
                        progressBar.show();
                        updateHelloChart();
                    }
                });



            }
        });

    }

    private void initControls() {


        sdf = new SimpleDateFormat("dd.MM.yyyy");
        tvHighestTemp = (TextView) findViewById(R.id.tvHighestTemp);
        tvLowestTemp = (TextView) findViewById(R.id.tvLowestTemp);
        tvAccTemp = (TextView) findViewById(R.id.tvAccTemp);
        tvYesterdayTemp = (TextView) findViewById(R.id.tvYesterdayTemp);


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

                if(!fabEnabled)
                {
                    fab.hide();
                }
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


        ib_zoom=(ImageButton)findViewById(R.id.ib_zoom);
        ib_zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helloController.zoomVertical();
            }
        });

        ib_zoomOut=(ImageButton)findViewById(R.id.ib_zoom_out);
        ib_zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helloController.zoomOutVertical();
            }
        });

        //initSeekBars();
    }

    public void setSelectedPointCardText(Temperature acc) {

        if (acc == null) {
            ((TextView) findViewById(R.id.tvSelctedTemp)).setText("N/A");
            ((TextView) findViewById(R.id.tvSelctedTempTime)).setText("N/A");
            return;
        }

        ((TextView) findViewById(R.id.tvSelctedTemp)).setText(round(acc.getTemp(), 1) + "°C");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        ((TextView) findViewById(R.id.tvSelctedTempTime)).setText(simpleDateFormat.format(acc.getTime()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!poolScreen) {
            navigationView.getMenu().getItem(0).setChecked(true);
            fabEnabled=true;
            poolScreen=true;
            fab.show();
            contentPanel.removeAllViews();
            contentPanel.addView(getLayoutInflater().inflate(R.layout.content_main,null));
            initSeekbars=!initSeekbars;
            initControls();
            helloController.setChart(helloChart);
            updateHelloChart();
        }
        else
            super.onBackPressed();
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
        item.setChecked(true);
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fabEnabled=true;
            poolScreen=true;
            fab.show();
            contentPanel.removeAllViews();
            contentPanel.addView(getLayoutInflater().inflate(R.layout.content_main,null));
            initSeekbars=!initSeekbars;
            initControls();
            helloController.setChart(helloChart);
            updateHelloChart();
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            poolScreen=false;
            fabEnabled=false;
            fab.hide();
            contentPanel.removeAllViews();
            contentPanel.addView(getLayoutInflater().inflate(R.layout.settings_layout,null));
            settingsActivity.initControls();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
