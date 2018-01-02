package com.example.lukas.pooltemp.RESTController;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.lukas.pooltemp.Model.Temperature;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;

/**
 * Created by wicki on 02.01.2018.
 */

public class TemperatureRestController {

    static final String REST_URL="http://pooltemp.ddns.net:8000/temperature";
    static final String WIFI_URL="http://192.168.0.179:8080/temperature";

    Context context;

    public TemperatureRestController(Context context) {
        this.context = context;
    }

    public void getAllTemps(Response.Listener<Temperature[]> listener, Response.ErrorListener errorListener){
        GsonRequest<Temperature[]> request = new GsonRequest<>(WIFI_URL,Temperature[].class,null,listener,errorListener);
                Volley.newRequestQueue(context).add(request);
    }

    public void getTempsSince(Date lastDate, Response.Listener<Temperature[]> listener, Response.ErrorListener errorListener){
        String url = WIFI_URL+"?since="+lastDate.getTime();
        GsonRequest<Temperature[]> request = new GsonRequest<>(url,Temperature[].class,null,listener,errorListener);
        Volley.newRequestQueue(context).add(request);
    }
}
