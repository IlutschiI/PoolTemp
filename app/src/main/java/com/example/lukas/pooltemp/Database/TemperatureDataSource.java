package com.example.lukas.pooltemp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lukas.pooltemp.Model.Temperature;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lukas on 15.08.2016.
 */
public class TemperatureDataSource {

    private SQLiteDatabase database;
    private SQLiteDBHelper dbHelper;
    private String[] allColumns = {SQLiteDBHelper.COLUMN_ID,SQLiteDBHelper.COLUMN_TEMP,SQLiteDBHelper.COLUMN_Date};

    public TemperatureDataSource(Context c) {
        dbHelper=new SQLiteDBHelper(c);
    }

    public void open(){
        database=dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public Temperature insertTemperature(Temperature temp){

        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.COLUMN_Date, temp.getTime().getTime());
        values.put(SQLiteDBHelper.COLUMN_TEMP, temp.getTemp());
        open();
        long rowId=database.insert(SQLiteDBHelper.TABLE_Temp,null,values);
        Cursor c = database.rawQuery("Select * from "+SQLiteDBHelper.TABLE_Temp+" where ROWID="+String.valueOf(rowId),null);
        c.moveToFirst();
        temp.setId(c.getLong(0));


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

        return temps;
    }

    public List<java.util.Date> getAllPossibleDates(){
        List<java.util.Date> result=new LinkedList<>();

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



        return result;
    }

    public void clearTable(){
        open();
        database.execSQL("Delete from "+SQLiteDBHelper.TABLE_Temp);
    }

    public List<Temperature> getTemps(java.util.Date startDate, java.util.Date endDate){

        List<Temperature> result=new LinkedList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");

        for (Temperature t :
                getAllTemperatures()) {

            try {
                java.util.Date date=sdf.parse((String)android.text.format.DateFormat.format("dd.MM.yyyy",t.getTime()));
                if(date.getTime()>=startDate.getTime()&&date.getTime()<=endDate.getTime())
                {
                    result.add(t);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


        }


        return result;
    }

    public int countEntries(){
        open();
        Cursor c = database.rawQuery("Select count(*) from "+SQLiteDBHelper.TABLE_Temp,null);
        c.moveToFirst();
        return c.getInt(0);
    }

    public Temperature getHighestTemperature(){

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_TEMP+" DESC", null);

        c.moveToFirst();

        Temperature t = new Temperature(c.getDouble(2),new java.util.Date(c.getLong(1)),c.getLong(0));

        return t;

    }

    public Temperature getLowestTemperature(){

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_TEMP, null);

        c.moveToFirst();

        Temperature t = new Temperature(c.getDouble(2),new java.util.Date(c.getLong(1)),c.getLong(0));

        return t;

    }

    public Temperature getAcctualTemperature(){

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_Date+" DESC", null);

        c.moveToFirst();

        Temperature t = new Temperature(c.getDouble(2),new java.util.Date(c.getLong(1)),c.getLong(0));

        return t;

    }
}
