package com.example.lukas.pooltemp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.Settings.Settings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lukas on 15.08.2016.
 */
public class TemperatureDataSource {

    static  int count=0;
    private SQLiteDatabase database;
    private SQLiteDBHelper dbHelper;
    private String[] allColumns = {SQLiteDBHelper.COLUMN_ID,SQLiteDBHelper.COLUMN_TEMP,SQLiteDBHelper.COLUMN_Date};
    private static TemperatureDataSource instance;

    public static TemperatureDataSource getInstance(Context context){
        if(instance==null)
        {
            instance=new TemperatureDataSource(context);
        }
        return instance;
    }

    private TemperatureDataSource(Context c) {
        dbHelper=new SQLiteDBHelper(c);
        open();
    }

    public void open(){
        database=dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public Temperature insertTemperature(Temperature temp){
        count++;
        System.out.println(count);
        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.COLUMN_Date, temp.getTime().getTime());
        values.put(SQLiteDBHelper.COLUMN_TEMP, temp.getTemp());
        open();
        long rowId=database.insert(SQLiteDBHelper.TABLE_Temp,null,values);
        Cursor c = database.rawQuery("Select * from "+SQLiteDBHelper.TABLE_Temp+" where ROWID="+String.valueOf(rowId),null);
        c.moveToFirst();

        temp.setId(c.getLong(0));
        c.close();
        close();


        return temp;
    }

    public List<Temperature> getAllTemperatures(){
        List<Temperature> temps=new LinkedList<>();

        if(countEntries()==0)
            return temps;

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_Date, null);

        c.moveToFirst();
        Temperature t;
        while(!c.isLast()){

            t = new Temperature(c.getDouble(2), new java.util.Date(c.getLong(1)),c.getLong(0));
            temps.add(t);
            c.moveToNext();
        }
        t = new Temperature(c.getDouble(2), new java.util.Date(c.getLong(1)),c.getLong(0));
        temps.add(t);
        close();
        return temps;
    }

    public List<java.util.Date> getAllPossibleDates(){
        /*List<java.util.Date> result=new LinkedList<>();

        List<Temperature> temps=getAllTemperatures();
        boolean contains=false;


        for (Temperature t :
                temps) {
            int yearInTemps= Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", t.getTime()));
            int monthInTemps= Integer.valueOf((String) android.text.format.DateFormat.format("MM", t.getTime()));
            int daysInTemps= Integer.valueOf((String) android.text.format.DateFormat.format("dd", t.getTime()));
            for (java.util.Date date :
                    result) {

                int yearInResult= Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", date));
                int monthInResult= Integer.valueOf((String) android.text.format.DateFormat.format("MM", date));
                int dayInMonth= Integer.valueOf((String) android.text.format.DateFormat.format("dd", date));

                if(yearInResult==yearInTemps&&monthInResult==monthInTemps&&dayInMonth==daysInTemps){
                    contains=true;
                }

            }
            if(!contains)
            {
                result.add(t.getTime());

            }
            contains=false;
        }



        return result;*/

        return generateDatebetween(getMinDate(),getMaxDate());
    }

    public void clearTable(){
        open();
        database.execSQL("Delete from "+SQLiteDBHelper.TABLE_Temp);
        close();
    }
    public List<Temperature> getTemps(java.util.Date startDate, java.util.Date endDate){

        List<Temperature> result=new LinkedList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");
        long s=startDate.getTime();
        long en=endDate.getTime();
        long d;

        for (Temperature t :
                getAllTemperatures()) {

            try {
                java.util.Date date2=sdf.parse((String)android.text.format.DateFormat.format("dd.MM.yyyy",t.getTime()));
                java.util.Date date=t.getTime();
                d=date.getTime();
                if(date.getTime()>=startDate.getTime()&&date.getTime()<=endDate.getTime())
                {
                    result.add(t);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }

        Temperature temp1;
        Temperature temp2;
        List<Temperature> minimizedList= new LinkedList<>();
        int numberPoints=Settings.getInstance().getPoolSettings().getNumberOfPoints();
        while (result.size()>= numberPoints)
        {
            minimizedList= new LinkedList<>();
            for (int i = 0;i<result.size();i=i+2){
                if(i+1<result.size()){
                    temp1=result.get(i);
                    temp2=result.get(i+1);
                    temp1.setTemp((temp1.getTemp()+temp2.getTemp())/2);
                    minimizedList.add(temp1);
                }
            }
/*
            temp1=result.get(0);
            temp2=result.get(1);
            result.remove(0);
            result.remove(0);
            temp1.setTemp((temp1.getTemp()+temp2.getTemp())/2);
            minimizedList.add(temp1);
            */
            result=minimizedList;
        }

        for (Temperature t :
                result) {
            t.setTemp(round(t.getTemp(),2));

        }

        return result;
    }

    public int countEntries(){
        open();
        Cursor c = database.rawQuery("Select count(*) from "+SQLiteDBHelper.TABLE_Temp,null);
        c.moveToFirst();
        int ret=c.getInt(0);
        close();
        return ret;
    }

    public Temperature getHighestTemperature(){

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_TEMP+" DESC", null);

        c.moveToFirst();
        if(!c.isAfterLast()) {
            Temperature t = new Temperature(round(c.getDouble(2), 1), new java.util.Date(c.getLong(1)), c.getLong(0));
            close();
            return t;
        }
        close();
        return null;

    }

    public Temperature getLowestTemperature(){

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_TEMP, null);

        c.moveToFirst();

        Temperature t = new Temperature(round(c.getDouble(2),1),new java.util.Date(c.getLong(1)),c.getLong(0));

        close();
        return t;

    }

    public Temperature getActualTemperature(){

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_Date+" DESC", null);

        c.moveToFirst();

        Temperature t = new Temperature(round(c.getDouble(2),1),new java.util.Date(c.getLong(1)),c.getLong(0));

        close();
        return t;

    }

    public int getDateRange(){

        List<java.util.Date> dates = getAllPossibleDates();

        return dates.size()-1;
        /*
        if(dates.size()==1)
            return 0;
        else
        {
            long diff=Math.abs(dates.get(0).getTime()-dates.get(dates.size()-1).getTime());

            return (int)(diff / (24 * 60 * 60 * 1000));
        }*/

    }

    public double getAverageOfYesterday(){

        open();
        Calendar calendar=GregorianCalendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);

        long startDate=calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);

        long endDate=calendar.getTimeInMillis();

        String query="Select AVG("+SQLiteDBHelper.COLUMN_TEMP+") from " + SQLiteDBHelper.TABLE_Temp +
                " where "+SQLiteDBHelper.COLUMN_Date+" >= "+startDate+" and "+SQLiteDBHelper.COLUMN_Date+" <= "+endDate;

        Cursor c = database.rawQuery(query, null);

        c.moveToFirst();
        double result=c.getDouble(0);
        close();
        return round(result,1);
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public java.util.Date getMinDate(){
        if(countEntries()==0)
            return new java.util.Date(Long.MIN_VALUE);

        open();
        Cursor c = database.rawQuery("Select Min("+SQLiteDBHelper.COLUMN_Date+") from " + SQLiteDBHelper.TABLE_Temp, null);

        c.moveToFirst();

        long ret=c.getLong(0);
        close();
        return new java.util.Date(ret);
    }

    public java.util.Date getMaxDate(){
        if(countEntries()==0)
            return new java.util.Date(Long.MIN_VALUE);

        open();
        Cursor c = database.rawQuery("Select Max("+SQLiteDBHelper.COLUMN_Date+") from " + SQLiteDBHelper.TABLE_Temp, null);

        c.moveToFirst();
        long ret=c.getLong(0);
        close();
        return new java.util.Date(ret);
    }

    public List<java.util.Date>generateDatebetween(java.util.Date minDate, java.util.Date maxDate){
        List<java.util.Date> dateList= new LinkedList<>();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(minDate);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);

        dateList.add(calendar.getTime());

        while (calendar.getTime().getTime()<maxDate.getTime()) {
            calendar.add(Calendar.DATE, 1);
            dateList.add(calendar.getTime());
        }
        dateList.remove(dateList.size()-1);
        return dateList;
    }
}
