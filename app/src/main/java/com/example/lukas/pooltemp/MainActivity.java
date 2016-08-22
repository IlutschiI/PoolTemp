package com.example.lukas.pooltemp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.format.DateUtils;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.example.lukas.pooltemp.Adapter.EndDateSpinnerAdapter;
import com.example.lukas.pooltemp.Adapter.StartDateSpinnerAdapter;
import com.example.lukas.pooltemp.Controller.LineChartController;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Initialize.Init;
import com.example.lukas.pooltemp.Model.Temperature;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    LineChartController controller;
    LineChartView chart;
    float[] data;
    Tooltip tip;
    FloatingActionButton fab;
    ScrollView scrollView;
    Spinner startDateSpinner;
    Spinner endDateSpinner;
    TemperatureDataSource TempSource;
    SimpleDateFormat sdf;
    boolean startDateSpinnerInitialized=false;
    boolean endDateSpinnerInitialized=false;
    StartDateSpinnerAdapter startDateSpinnerAdapter;
    EndDateSpinnerAdapter endDateSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        sdf= new SimpleDateFormat("dd.MM.yyyy");
        setSupportActionBar(toolbar);
        startDateSpinner =(Spinner)findViewById(R.id.startDateSpinner); //Spinner für StartDate
        endDateSpinner =(Spinner)findViewById(R.id.endDateSpinner);     //Spinner für EndDate

        startDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!startDateSpinnerInitialized){
                    startDateSpinnerInitialized=true;
                    return;
                }

                updateChartsRange();

                Date sDate=null;
                if (startDateSpinner.getSelectedItem().toString().equals("Datum auswählen")) {
                    sDate = new Date(Long.MIN_VALUE);
                }
                else {
                    try {
                        sDate = sdf.parse(sdf.format(startDateSpinner.getSelectedItem()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                List<String>dateList=new LinkedList<>();
                dateList.add("Datum auswählen");
                for (Date d:
                        TempSource.getAllPossibleDates()) {

                    if(d.getTime()>=sDate.getTime()) {
                        int yearInResult = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", d));
                        int monthInResult = Integer.valueOf((String) android.text.format.DateFormat.format("MM", d));
                        int dayInMonth = Integer.valueOf((String) android.text.format.DateFormat.format("dd", d));

                        dateList.add("" + dayInMonth + "." + monthInResult + "." + yearInResult);
                    }

                }
                ArrayAdapter<String> spinneradapter=new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,dateList);
                spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



                //endDateSpinnerInitialized=false;
                //endDateSpinner.setAdapter(spinneradapter);
                endDateSpinnerAdapter.setMinDate((Date)startDateSpinner.getSelectedItem());
        }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        endDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!endDateSpinnerInitialized){
                    endDateSpinnerInitialized=true;
                    return;
                }
                updateChartsRange();

                Date sDate=null;
                if (endDateSpinner.getSelectedItem().toString().equals("Datum auswählen")) {
                    sDate = new Date(Long.MAX_VALUE);
                }
                else {
                    try {
                        sDate = sdf.parse(sdf.format((Date)startDateSpinner.getSelectedItem()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                List<String>dateList=new LinkedList<>();
                dateList.add("Datum auswählen");
                for (Date d:
                        TempSource.getAllPossibleDates()) {

                    if(d.getTime()<=sDate.getTime()) {
                        int yearInResult = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", d));
                        int monthInResult = Integer.valueOf((String) android.text.format.DateFormat.format("MM", d));
                        int dayInMonth = Integer.valueOf((String) android.text.format.DateFormat.format("dd", d));

                        dateList.add("" + dayInMonth + "." + monthInResult + "." + yearInResult);
                    }

                }
                ArrayAdapter<String> spinneradapter=new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_spinner_item,dateList);
                spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //startDateSpinnerInitialized=false;
                //startDateSpinner.setAdapter(spinneradapter);
                startDateSpinnerAdapter.setMinDate((Date)endDateSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        fab = (FloatingActionButton) findViewById(R.id.fab);    //FloatingActionButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                      //Onclicklistener des FloatingActionButtons
               if (!controller.isanimating())
                    controller.addPoint(new Point("", 25));

            }
        });


        TextView tv  = (TextView)findViewById(R.id.mainTV);
        TempSource= new TemperatureDataSource(this);
        Init.initDB(TempSource);



        List<Temperature> temps=TempSource.getAllTemperatures();
        data= new float[temps.size()];
        for (int i = 0; i<temps.size();i++) {
            tv.setText(tv.getText() +"\n "+temps.get(i).toString());
            data[i]=(float)temps.get(i).getTemp();
        }
        tv.setText(tv.getText() + "\n \nEs sind " + TempSource.countEntries() + " Einträge vorhanden");

        controller = new LineChartController(this,(LineChartView)findViewById(R.id.linechart));
        controller.initChart();
        LineSet line = new LineSet(new String[] {"10","20","30","40","50","","","","","",""},data);

        line.setFill(Color.parseColor("#311B92"))
                .setSmooth(true)
                .setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.CYAN);
        controller.addLine(line);
        controller.showChart();

        //Scrollview
        //Der FAB wird, wenn hinutergescrolled wird, ausgeblendet
        scrollView=((ScrollView)findViewById(R.id.scrollview));
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int i=scrollView.getScrollY();

                if (i>=130)
                {
                    fab.hide();
                }
                else
                    fab.show();
            }
        });

        List<String>dateList=new LinkedList<>();
        dateList.add("Datum auswählen");
        for (Date d:
             TempSource.getAllPossibleDates()) {


            int yearInResult= Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", d));
            int monthInResult= Integer.valueOf((String) android.text.format.DateFormat.format("MM", d));
            int dayInMonth= Integer.valueOf((String) android.text.format.DateFormat.format("dd", d));

            dateList.add(""+dayInMonth+"."+monthInResult+"."+yearInResult);

        }
        ArrayAdapter<String> spinneradapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,dateList);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startDateSpinnerAdapter=new StartDateSpinnerAdapter(this,TempSource.getAllPossibleDates());
        endDateSpinnerAdapter=new EndDateSpinnerAdapter(this,TempSource.getAllPossibleDates());


        startDateSpinner.setAdapter(startDateSpinnerAdapter);
        endDateSpinner.setAdapter(endDateSpinnerAdapter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    public void updateChartsRange(){

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date sDate=null;
        Date eDate=null;
        String s = startDateSpinner.getSelectedItem().toString();
        try {
            if (((TextView)endDateSpinner.getSelectedView().findViewById(R.id.spinnerItem)).getText().equals("Datum auswählen")) {
                eDate = new Date(Long.MAX_VALUE);
            } else
                eDate =sdf.parse(sdf.format((Date)endDateSpinner.getSelectedItem()));


            if (((TextView)startDateSpinner.getSelectedView().findViewById(R.id.spinnerItem)).getText().equals("Datum auswählen")) {
                sDate = new Date(Long.MIN_VALUE);
            }
            else
                sDate = sdf.parse(sdf.format((Date)startDateSpinner.getSelectedItem()));
        }
        catch(Exception e){}

        if(sDate.getTime()==new Date(Long.MAX_VALUE).getTime())
            sDate=new Date(Long.MIN_VALUE);
        if(eDate.getTime()==new Date(Long.MIN_VALUE).getTime())
            sDate=new Date(Long.MAX_VALUE);

        List<Temperature>data=TempSource.getTemps(sDate, eDate);

        String[] labels= new String[data.size()];
        float[] values=new float[labels.length];

        for (int i =0;i<labels.length;i++){
            values[i]=(float)data.get(i).getTemp();
            labels[i]="";
        }
        int year= Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", data.get(0).getTime()));
        int month= Integer.valueOf((String) android.text.format.DateFormat.format("MM", data.get(0).getTime()));
        int day= Integer.valueOf((String) android.text.format.DateFormat.format("dd", data.get(0).getTime()));
        labels[0]=""+day+"."+month+"."+year;


        year= Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", data.get(data.size()-1).getTime()));
        month= Integer.valueOf((String) android.text.format.DateFormat.format("MM", data.get(data.size()-1).getTime()));
        day= Integer.valueOf((String) android.text.format.DateFormat.format("dd", data.get(data.size()-1).getTime()));
        labels[labels.length-1]=""+day+"."+month+"."+year;

        final LineSet line = new LineSet(labels,values);
        line.setFill(Color.parseColor("#311B92"))
                .setSmooth(true)
                .setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.CYAN);
        endDateSpinner.setEnabled(false);
        startDateSpinner.setEnabled(false);

                controller.addLine(line);
                controller.showChart(new Runnable() {
                    @Override
                    public void run() {
                        endDateSpinner.setEnabled(true);
                        startDateSpinner.setEnabled(true);

                    }
                });


    }


    /*
        Fab wird Enabled bzw. Disabled und die Farbe wird dementsprechend geändert
     */
    public void setFabEnabled(boolean b){
        Drawable background=fab.getBackground();
        int alpha=background.getAlpha();
        fab.setEnabled(b);
        if(b) {


            //fab.setBackgroundColor(Color.parseColor("#FF4081"));
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF4081")));

        }
        else
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#93264B")));

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
        if (id == R.id.action_settings) {
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
}
