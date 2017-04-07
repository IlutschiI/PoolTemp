package com.example.lukas.pooltemp.RESTController;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Activitys.MainActivity;
import com.example.lukas.pooltemp.Fragments.PoolTempFragment;
import com.example.lukas.pooltemp.Model.Temperature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lukas on 21.08.2016.
 */
public class RestController {

    static final String REST_URL="http://pooltemp.ddns.net:8000/PoolTempServer/webresources/TemperatureREST";
    static final String WIFI_URL="http://192.168.0.65:8080/PoolTempServer/webresources/TemperatureREST";


    public static void getAllTemps(final MainActivity c,final PoolTempFragment pf){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, REST_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {



                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                        temperatureDataSource.clearTable();

                        if(response.length()==0) {
                            pf.updateHelloChart();
                            return;
                        }

                        System.out.println(response);
                        JSONObject jsonObject;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        double temp;
                        Date time;
                        String t="";
                        Temperature temperature;
                        List<Temperature> temperatureList=new LinkedList<Temperature>();
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                jsonObject = response.getJSONObject(i);

                                temp=jsonObject.getDouble("temperature");
                                t=jsonObject.getString("time").substring(0,19);
                                time=sdf.parse(jsonObject.getString("time").substring(0,19));
                                temperature=new Temperature(temp,time);
//                                temperatureDataSource.insertTemperature(temperature);
                                temperatureList.add(temperature);
//                                c.updateProgress(response.length(),i-1);


                            }
                            temperatureDataSource.insertTemperatureMany(temperatureList, c);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();

                        }
                        // Toast.makeText(c,t,Toast.LENGTH_LONG).show();



                        pf.refreshPossibleDates();
                        pf.updateHelloChart();
                        c.resetProgress();
                    }
                }).start();



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();
                pf.updateHelloChart();
                c.resetProgress();
            }
        });

        Volley.newRequestQueue(c).add(request);
    }

    public static void getTempsSince(final MainActivity c, final Date lastDate, final PoolTempFragment pf){
        String url=REST_URL+"/"+lastDate.getTime();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                        //temperatureDataSource.clearTable();

                        if(response.length()==0) {
                            pf.updateHelloChart();
                            return;
                        }

                        System.out.println(response);
                        JSONObject jsonObject;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        double temp;
                        Date time;
                        String t="";
                        Temperature temperature;
                        try {
                            for (int i = 1; i < response.length(); i++) {

                                jsonObject = response.getJSONObject(i);

                                temp=jsonObject.getDouble("temperature");
                                t=jsonObject.getString("time").substring(0,19);
                                time=sdf.parse(jsonObject.getString("time").substring(0,19));
                                temperature=new Temperature(temp,time);
                                temperatureDataSource.insertTemperature(temperature);
                                c.updateProgress(response.length(),i-1);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();

                        }
                        // Toast.makeText(c,t,Toast.LENGTH_LONG).show();
                        pf.refreshPossibleDates();
                        pf.updateHelloChart();
                        c.resetProgress();
                    }
                }).start();




            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();
                //c.updateHelloChart();
                Toast.makeText(c.getBaseContext(),"Daten konnten nicht aktualisiert werden \n Daten werden über Wifi abgerufen",Toast.LENGTH_LONG).show();
                getTempsSinceOverWifi(c, lastDate,pf);
            }
        });
        RequestQueue queue= Volley.newRequestQueue(c);
        queue.add(request);
    }

    public static void forceNewTemperature(final MainActivity c, final PoolTempFragment pf){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, REST_URL+"/forceTemperature", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject response) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);


                        if(response.length()==0) {
                            pf.updateHelloChart();
                            return;
                        }

                        System.out.println(response);
                        JSONObject jsonObject;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        double temp;
                        Date time;
                        String t="";
                        //c.setProgressForForceNewTemp();
                        Temperature temperature;
                        try {


                            jsonObject = response;

                            temp=jsonObject.getDouble("temperature");
                            t=jsonObject.getString("time").substring(0,19);
                            time=sdf.parse(jsonObject.getString("time").substring(0,19));
                            temperature=new Temperature(temp,time);
                            temperatureDataSource.insertTemperature(temperature);




                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();

                        }
                        // Toast.makeText(c,t,Toast.LENGTH_LONG).show();
                        pf.updateHelloChart();
                        c.resetProgress();
                    }
                }).start();



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();
                pf.refreshPossibleDates();
                pf.updateHelloChart();
                c.resetProgress();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(5000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(c).add(request);
    }

    public static void getTempsSinceOverWifi(final MainActivity c, Date lastDate, final PoolTempFragment pf){
        String url=WIFI_URL+"/"+lastDate.getTime();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();

                if(response.length()==0) {
                    pf.updateHelloChart();
                    return;
                }

                System.out.println(response);
                JSONObject jsonObject;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                double temp;
                Date time;
                String t="";
                Temperature temperature;
                try {
                    for (int i = 1; i < response.length(); i++) {

                        jsonObject = response.getJSONObject(i);

                        temp=jsonObject.getDouble("temperature");
                        t=jsonObject.getString("time").substring(0,19);
                        time=sdf.parse(jsonObject.getString("time").substring(0,19));
                        temperature=new Temperature(temp,time);
                        temperatureDataSource.insertTemperature(temperature);
                        c.updateProgress(response.length(),i-1);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();

                }
                 Toast.makeText(c,"Daten wurde über WIFI abgerufen",Toast.LENGTH_LONG).show();
                c.resetProgress();
                pf.refreshPossibleDates();
                pf.updateHelloChart();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();
                pf.updateHelloChart();
                Toast.makeText(c.getBaseContext(),"Daten konnten nicht aktualisiert werden",Toast.LENGTH_LONG).show();
                c.resetProgress();
            }
        });
        RequestQueue queue= Volley.newRequestQueue(c);
        queue.add(request);
    }

    public void runOnUiThread(Runnable r) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(r);
    }
}
