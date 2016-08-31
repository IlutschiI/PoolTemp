package com.example.lukas.pooltemp.RESTController;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.MainActivity;
import com.example.lukas.pooltemp.Model.Temperature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lukas on 21.08.2016.
 */
public class RestController {

    static final String REST_URL="http://pooltemp.ddns.net:8000/PoolTempServer/webresources/TemperatureREST";


    public static void getAllTemps(final MainActivity c){
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, REST_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                temperatureDataSource.clearTable();

                if(response.length()==0) {
                    c.updateHelloChart();
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
                    for (int i = 0; i < response.length(); i++) {

                        jsonObject = response.getJSONObject(i);

                        temp=jsonObject.getDouble("temperature");
                        t=jsonObject.getString("time").substring(0,19);
                        time=sdf.parse(jsonObject.getString("time").substring(0,19));
                        temperature=new Temperature(temp,time);
                        temperatureDataSource.insertTemperature(temperature);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();

                }
                // Toast.makeText(c,t,Toast.LENGTH_LONG).show();
                c.updateHelloChart();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                temperatureDataSource.clearTable();
                c.updateHelloChart();
            }
        });

        Volley.newRequestQueue(c).add(request);
    }

    public static void getTempsSince(final MainActivity c, Date lastDate){
        String url=REST_URL+"/"+lastDate.getTime();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();

                if(response.length()==0) {
                    c.updateHelloChart();
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


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();

                }
                // Toast.makeText(c,t,Toast.LENGTH_LONG).show();
                c.updateHelloChart();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TemperatureDataSource temperatureDataSource = TemperatureDataSource.getInstance(c);
                //temperatureDataSource.clearTable();
                c.updateHelloChart();
                Toast.makeText(c.getBaseContext(),"Daten konnten nicht aktualisiert werden",Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue queue= Volley.newRequestQueue(c);
        queue.add(request);
    }

}
