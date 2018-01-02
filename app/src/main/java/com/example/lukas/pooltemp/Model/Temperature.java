package com.example.lukas.pooltemp.Model;

import java.util.Date;

/**
 * Created by Lukas on 15.08.2016.
 */
public class Temperature {

    private long id;
    private double temperature;
    private Date time;
    private String sensorID;

    public Temperature(double temperature, Date time) {
        this.temperature = temperature;
        this.time = time;
    }

    public Temperature(double temperature, Date time, long id) {
        this.temperature = temperature;
        this.time = time;
        this.id = id;
    }

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return time.toString()+"   "+ temperature;
    }
}
