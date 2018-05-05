package com.example.lukas.pooltemp.Activitys;


import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Fragments.PoolTempFragment;
import com.example.lukas.pooltemp.Fragments.SettingsFragment;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.R;
import com.example.lukas.pooltemp.RESTController.RestController;
import com.example.lukas.pooltemp.RESTController.TemperatureRestController;
import com.example.lukas.pooltemp.Settings.Settings;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    TemperatureDataSource TempSource;
    public static MainActivity instance;
    ProgressBar progress;
    AlertDialog progressDialog;
    TextView progressText;
    FragmentManager fm;
    PoolTempFragment poolFragment;
    SettingsFragment settingsFragment;
    TemperatureRestController temperatureRestController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("---------------------------------------System start----------------------------------");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        temperatureRestController=new TemperatureRestController(this.getApplicationContext());

        //creating the Fragments
        poolFragment = new PoolTempFragment();
        settingsFragment = new SettingsFragment();
        //updating the stored Settings
        settingsFragment.updateSettings();

        //Set the first Fragment (default PoolTempFragment)
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentFrame, poolFragment);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.commit();

        //Loading Data from the Server when no Data is available
        loadDataOnFirstStart();

        //setting the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setting the Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initializeProgressDialog();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initializeProgressDialog() {
        progressDialog = new AlertDialog.Builder(this).setTitle("Bitte warten")
                .setView(R.layout.progress_dialog_progress)
                .setCancelable(false)
                .create();
        progressDialog.show();
        progress = (ProgressBar) progressDialog.findViewById(R.id.pbProgressDialogProgress);
        progressText = (TextView) progressDialog.findViewById(R.id.tvProgressDialogProgress);
        progress.setMax(11);
        progress.setIndeterminate(false);

        progress.setProgress(10);
        progress.setProgress(0);
        resetProgress();
    }

    private void loadDataOnFirstStart() {
        TempSource = TemperatureDataSource.getInstance(this);
        if (TempSource.countEntries() != 0) {
        } else {
            temperatureRestController.getAllTemps(new Response.Listener<Temperature[]>() {
                @Override
                public void onResponse(Temperature[] response) {
                    TempSource.insertTemperatureMany(Arrays.asList(response),instance);
                    poolFragment.updateMPChart(true);
                    poolFragment.refreshPossibleDates();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(instance, "somewthing went wrong", Toast.LENGTH_SHORT).show();
                    resetProgress();
                }
            });
        }
    }

    //Updating the Values of the ProgressDialog
    public void updateProgress(final int max, final int prog) {
        //be sure to run on the Ui-Thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (max == -1) {
                    progressDialog.show();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    return;
                }

                if (prog != progress.getMax())
                    progress.setMax(max);
                progress.setProgress(prog);
                if (!progressDialog.isShowing())
                    progressDialog.show();
                progressText.setText(prog + "/" + max);
            }
        });

    }

    //resetting the ProgressDialog
    public void resetProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(0);
                progressText.setText("--/--");
                progressDialog.dismiss();
                progress.setIndeterminate(false);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        });

    }

    public void setProgressForForceNewTemp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                progress.setIndeterminate(true);
                progressText.setText("Lade aktuelle Temperatur");
                progressDialog.show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Fragment fragment = fm.findFragmentById(R.id.fragmentFrame);
            if (fragment instanceof PoolTempFragment) {
                navigationView.getMenu().getItem(0).setChecked(true);
            } else
                navigationView.getMenu().getItem(3).setChecked(true);

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

        //Reloading all Temperatures from the Server
        if (id == R.id.action_reload) {
            progressDialog.show();
            temperatureRestController.getAllTemps(new Response.Listener<Temperature[]>() {
                @Override
                public void onResponse(Temperature[] response) {
                    TempSource.clearTable();
                    TempSource.insertTemperatureMany(Arrays.asList(response),instance);
                    poolFragment.updateMPChart(true);
                    poolFragment.refreshPossibleDates();
                    resetProgress();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(instance, "somewthing went wrong", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        //force the Server to read a new Tmeperature
        if (id == R.id.action_forceTemperature) {
            setProgressForForceNewTemp();
            RestController.forceNewTemperature(instance, poolFragment);
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

        //setting the PoolFragment
        if (id == R.id.nav_pool) {

            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.addToBackStack(null);
            ft.replace(R.id.fragmentFrame, poolFragment, "PoolTempFragment");
            poolFragment.refreshPossibleDates();


        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        }
        //setting the SettingsFragment
        else if (id == R.id.nav_manage) {

            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.addToBackStack(null);
            ft.replace(R.id.fragmentFrame, settingsFragment, "SettingsFragment");

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        //finishing the Fragment change
        ft.commit();

        //closing the Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
