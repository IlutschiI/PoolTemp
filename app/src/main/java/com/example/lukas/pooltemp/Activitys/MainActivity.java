package com.example.lukas.pooltemp.Activitys;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.view.Tooltip;
import com.example.lukas.pooltemp.Adapter.EndDateSpinnerAdapter;
import com.example.lukas.pooltemp.Adapter.LockableScrollView;
import com.example.lukas.pooltemp.Adapter.StartDateSpinnerAdapter;
import com.example.lukas.pooltemp.Controller.HelloChartController;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Fragments.PoolTempFragment;
import com.example.lukas.pooltemp.Fragments.SettingsFragment;
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
    Spinner startDateSpinner;
    Spinner endDateSpinner;
    TemperatureDataSource TempSource;

    boolean startDateSpinnerInitialized = false;
    boolean endDateSpinnerInitialized = false;

    StartDateSpinnerAdapter startDateSpinnerAdapter;
    EndDateSpinnerAdapter endDateSpinnerAdapter;
    public static MainActivity instance;
    boolean poolScreen=true;
    List<Date> possibleDates;
    //LinearLayout contentPanel;
    SettingsActivity settingsActivity;
    ProgressBar progress;
    AlertDialog progressDialog;
    TextView progressText;
    List<Temperature> data;

    FragmentManager fm;


    PoolTempFragment poolFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("---------------------------------------System start----------------------------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //contentPanel=(LinearLayout) findViewById(R.id.contentPanel);
        //contentPanel.addView(getLayoutInflater().inflate(R.layout.content_main,null));

        poolFragment=new PoolTempFragment();
        fm = getSupportFragmentManager();
        FragmentTransaction fs=fm.beginTransaction();
        fs.replace(R.id.fragmentFrame,poolFragment);

        instance = this;



        settingsActivity=new SettingsActivity(instance);
        settingsActivity.updateSettings();
        if(Settings.getInstance().getPoolSettings().getNumberOfPoints()==0)
            Settings.getInstance().getPoolSettings().setNumberOfPoints(100);


        TempSource = TemperatureDataSource.getInstance(this);
        if (TempSource.countEntries() != 0) {
            //RestController.getTempsSince(instance, TempSource.getActualTemperature().getTime());
        } else {
            RestController.getAllTemps(instance,poolFragment);
            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!before all Dates");
        possibleDates = TempSource.getAllPossibleDates();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!after all Dates");


        progressDialog=new AlertDialog.Builder(this).setTitle("Bitte warten")
                .setView(R.layout.progress_dialog_progress)
                .setCancelable(false)
                .create();
        progressDialog.show();



        progress=(ProgressBar) progressDialog.findViewById(R.id.pbProgressDialogProgress);
        progressText=(TextView) progressDialog.findViewById(R.id.tvProgressDialogProgress);
        progress.setMax(11);
        progress.setIndeterminate(false);

        progress.setProgress(10);
        progress.setProgress(0);
        progressDialog.dismiss();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });





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


*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }








    public void updateProgress(final int max,final int prog){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(prog!=progress.getMax())
                    progress.setMax(max);
                progress.setProgress(prog);

                progressText.setText(prog+"/"+max);
            }
        });

    }

    public void resetProgress(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(0);
                progressText.setText("--/--");
            }
        });

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!poolScreen) {
            /*navigationView.getMenu().getItem(0).setChecked(true);
            fabEnabled=true;
            poolScreen=true;
            fab.show();
            contentPanel.removeAllViews();
            contentPanel.addView(getLayoutInflater().inflate(R.layout.content_main,null));
            initSeekbars=!initSeekbars;
            initControls();
            helloController.setChart(helloChart);
            updateHelloChart();*/
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
            progressDialog.show();
            RestController.getAllTemps(instance,poolFragment);
            return true;
        }

        if (id == R.id.action_forceTemperature) {
            progressDialog.show();
            RestController.forceNewTemperature(instance,poolFragment);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        item.setChecked(true);
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            /*fabEnabled=true;
            poolScreen=true;
            fab.show();
            contentPanel.removeAllViews();
            contentPanel.addView(getLayoutInflater().inflate(R.layout.content_main,null));
            initSeekbars=!initSeekbars;
            initControls();
            helloController.setChart(helloChart);
            updateHelloChart();
            // Handle the camera action*/
            ft.replace(R.id.fragmentFrame,new PoolTempFragment(),"PoolTempFragment");



        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            /*poolScreen=false;
            fabEnabled=false;
            fab.hide();
            contentPanel.removeAllViews();
            contentPanel.addView(getLayoutInflater().inflate(R.layout.settings_layout,null));
            settingsActivity.initControls();*/
            ft.replace(R.id.fragmentFrame,new SettingsFragment(),"SettingsFragment");
            //startActivity(new Intent(this,SettingsFragment.class));

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        ft.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}
