package com.example.lukas.pooltemp;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.lukas.pooltemp.RESTController.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    MainActivity instance;
    TextView tvHighestTemp;
    TextView tvLowestTemp;
    TextView tvAccTemp;
    CardView ttSeekbar;
    SeekBar sbTime;
    CardView ttSeekbarEnd;
    SeekBar sbTimeEnd;
    ProgressDialog progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance=this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        sdf= new SimpleDateFormat("dd.MM.yyyy");
        setSupportActionBar(toolbar);
        startDateSpinner =(Spinner)findViewById(R.id.startDateSpinner); //Spinner für StartDate
        endDateSpinner =(Spinner)findViewById(R.id.endDateSpinner);     //Spinner für EndDate
        tvHighestTemp=(TextView)findViewById(R.id.tvHighestTemp);
        tvLowestTemp=(TextView)findViewById(R.id.tvLowestTemp);
        tvAccTemp=(TextView)findViewById(R.id.tvAccTemp);

        progressBar=new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Daten werden aktualisiert ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(-1);

        initSeekBars();

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
                String s = endDateSpinner.getSelectedItem().toString();
                Toast.makeText(getBaseContext(),s,Toast.LENGTH_LONG).show();
                if (s.equals("Datum auswählen")) {
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
               //if (!controller.isanimating())
                 //   controller.addPoint(new Point("", 25));
                fab.setEnabled(false);
                progressBar.show();
                RestController.getTempsSince(instance,TempSource.getAcctualTemperature().getTime());

            }
        });


        TextView tv  = (TextView)findViewById(R.id.mainTV);
        TempSource= new TemperatureDataSource(this);
        //Init.initDB(TempSource);

        controller = new LineChartController(this,(LineChartView)findViewById(R.id.linechart));
        controller.initChart();

        progressBar.show();

        RestController.getAllTemps(instance);

/*
        List<Temperature> temps=TempSource.getAllTemperatures();
        data= new float[temps.size()];
        for (int i = 0; i<temps.size();i++) {
            tv.setText(tv.getText() +"\n "+temps.get(i).toString());
            data[i]=(float)temps.get(i).getTemp();
        }
        tv.setText(tv.getText() + "\n \nEs sind " + TempSource.countEntries() + " Einträge vorhanden");


        LineSet line = new LineSet(new String[] {"10","20","30","40","50","","","","","",""},data);

        line.setFill(Color.parseColor("#311B92"))
                .setSmooth(true)
                .setColor(Color.parseColor("#758cbb"))
                .setDotsColor(Color.CYAN);
        //controller.addLine(line);
        //controller.showChart();
*/
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

        if(!progressBar.isShowing())
            progressBar.show();

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
                sDate = new Date(0);
            }
            else
                sDate = sdf.parse(sdf.format((Date)startDateSpinner.getSelectedItem()));
        }
        catch(Exception e){
            e.printStackTrace();
        }

        if(sDate==null||sDate.getTime()==new Date(Long.MAX_VALUE).getTime())
            sDate=new Date(Long.MIN_VALUE);
        if(eDate==null||eDate.getTime()==new Date(Long.MIN_VALUE).getTime())
            eDate=new Date(Long.MAX_VALUE);

        Calendar startCalendar=new GregorianCalendar();
        startCalendar.setTime(sDate);
        startCalendar.set(Calendar.HOUR_OF_DAY,sbTime.getProgress());
        startCalendar.set(Calendar.MINUTE,0);
        startCalendar.set(Calendar.SECOND,0);
        sDate=startCalendar.getTime();

        if(eDate.getTime()!=Long.MAX_VALUE){
            Calendar endCalendar = new GregorianCalendar();
            endCalendar.setTime(eDate);
            endCalendar.set(Calendar.HOUR_OF_DAY, sbTimeEnd.getProgress());
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            eDate = endCalendar.getTime();
        }

        List<Temperature>data=TempSource.getTemps(sDate, eDate);

        if(data.size()==0){
            setInfoCardText();
            setFabEnabled(false);
            controller.setEnabled(false);
            controller.addLine(new LineSet(new String[]{""},new float[]{0}));
            controller.showChart(new Runnable() {
                @Override
                public void run() {
                    endDateSpinner.setEnabled(true);
                    startDateSpinner.setEnabled(true);

                }
            });
            progressBar.dismiss();
            return;
        }
        else{

            controller.setEnabled(true);
        }

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
                .setDotsColor(Color.CYAN)
                .setDotsRadius(5);
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

        setInfoCardText();

        tvHighestTemp.setText(""+TempSource.getHighestTemperature().getTemp()+"°C");
        tvLowestTemp.setText(""+TempSource.getLowestTemperature().getTemp()+"°C");
        tvAccTemp.setText(""+TempSource.getAcctualTemperature().getTemp()+"°C");
        startDateSpinnerAdapter.setData(TempSource.getAllPossibleDates());
        endDateSpinnerAdapter.setData(TempSource.getAllPossibleDates());

        Thread t =new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressBar.dismiss();
            }
        });
        t.start();


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

    public void setInfoCardText(){
        try {
            tvHighestTemp.setText(""+TempSource.getHighestTemperature().getTemp()+"°C");
        }
        catch (Exception e){
            tvHighestTemp.setText("N/A");
        }

        try {
            tvLowestTemp.setText(""+TempSource.getLowestTemperature().getTemp()+"°C");
        }
        catch (Exception e){
            tvLowestTemp.setText("N/A");
        }

        try {
            tvAccTemp.setText(""+TempSource.getAcctualTemperature().getTemp()+"°C");
        }
        catch (Exception e){
            tvAccTemp.setText("N/A");
        }
    }

    public void initSeekBars(){
        ttSeekbar=(CardView)findViewById(R.id.ttSeekbar);
        sbTime=(SeekBar)findViewById(R.id.sbTime);
        sbTime.setMax(24);
        sbTime.incrementProgressBy(1);
        sbTime.setProgress(0);

        sbTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==motionEvent.ACTION_DOWN)
                    ttSeekbar.setVisibility(View.VISIBLE);
                // else
                //     ttSeekbar.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        sbTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i>sbTimeEnd.getProgress())
                {
                    seekBar.setProgress(i-1);
                    return;
                    // Toast.makeText(getBaseContext(),"not allowed",Toast.LENGTH_LONG).show();
                }

                int x=seekBar.getThumb().getBounds().left;
                ((TextView)ttSeekbar.findViewById(R.id.ttSeekbarValue)).setText(i+":00");
                ttSeekbar.setX(x);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ttSeekbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ttSeekbar.setVisibility(View.INVISIBLE);
                updateChartsRange();
            }
        });

        ttSeekbarEnd=(CardView)findViewById(R.id.ttSeekbarEnd);
        sbTimeEnd=(SeekBar)findViewById(R.id.sbTimeEnd);
        sbTimeEnd.setMax(24);
        sbTimeEnd.setProgress(24);
        sbTimeEnd.incrementProgressBy(1);
        sbTimeEnd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==motionEvent.ACTION_DOWN)
                    ttSeekbarEnd.setVisibility(View.VISIBLE);
                // else
                //     ttSeekbar.setVisibility(View.INVISIBLE);
                return false;
            }
        });
        sbTimeEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<sbTime.getProgress())
                {
                    seekBar.setProgress(i+1);
                    return;
                   // Toast.makeText(getBaseContext(),"not allowed",Toast.LENGTH_LONG).show();
                }

                int x=seekBar.getThumb().getBounds().left;
                ((TextView)ttSeekbarEnd.findViewById(R.id.ttSeekbarEndValue)).setText(i+":00");
                ttSeekbarEnd.setX(x);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ttSeekbarEnd.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ttSeekbarEnd.setVisibility(View.INVISIBLE);
                updateChartsRange();
            }
        });
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
