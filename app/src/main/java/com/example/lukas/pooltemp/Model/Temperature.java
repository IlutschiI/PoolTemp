package com.example.lukas.pooltemp.Model;

import java.util.Date;

/**
 * Created by Lukas on 15.08.2016.
 */
public class Temperature {

    private double temp;
    private Date time;
    private long id;

    public Temperature(double temp, Date time) {
        this.temp = temp;
        this.time = time;
    }

    public Temperature(double temp, Date time, long id) {
        this.temp = temp;
        this.time = time;
        this.id = id;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
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
        return time.toString()+"   "+temp;
    }
}
