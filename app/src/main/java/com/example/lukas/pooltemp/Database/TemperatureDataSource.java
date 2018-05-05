package com.example.lukas.pooltemp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.lukas.pooltemp.Activitys.MainActivity;
import com.example.lukas.pooltemp.Model.Temperature;
import com.example.lukas.pooltemp.Settings.Settings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lukas on 15.08.2016.
 */
public class TemperatureDataSource {

    static int count = 0;
    private SQLiteDatabase database;
    private SQLiteDBHelper dbHelper;
    SQLiteStatement statement;
    private String[] allColumns = {SQLiteDBHelper.COLUMN_ID, SQLiteDBHelper.COLUMN_TEMP, SQLiteDBHelper.COLUMN_Date};
    private static TemperatureDataSource instance;

    public static TemperatureDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new TemperatureDataSource(context);
        }
        return instance;
    }

    private TemperatureDataSource(Context c) {
        dbHelper = new SQLiteDBHelper(c);
        open();
    }

    public void open() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
            database.rawQuery("PRAGMA synchronous = OFF", null);
            database.rawQuery("PRAGMA journal_mode = MEMORY", null);
        }
    }

    public void close() {
        dbHelper.close();
    }

    public Temperature insertTemperature(Temperature temp) {
        count++;
//        System.out.println(count);
        ContentValues values = new ContentValues();
        values.put(SQLiteDBHelper.COLUMN_Date, temp.getTime().getTime());
        values.put(SQLiteDBHelper.COLUMN_TEMP, temp.getTemperature());
        values.put(SQLiteDBHelper.COLUMN_SENSOR, temp.getSensorID());
        open();
        long rowId = database.insert(SQLiteDBHelper.TABLE_Temp, null, values);
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " where ROWID=" + String.valueOf(rowId), null);
        c.moveToFirst();

        temp.setId(c.getLong(0));
        c.close();
        //close();


        return temp;
    }

    public Temperature insertTemperatureWithPreparedStatement(Temperature temp) {
        count++;
        open();

        statement.bindDouble(1, temp.getTemperature());
        statement.bindLong(2, temp.getTime().getTime());
        if (temp.getSensorID() == null) {
            statement.bindString(3, "");
        } else {
            statement.bindString(3, temp.getSensorID());
        }
        long rowId = statement.executeInsert();
//        System.out.println(count);
//        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " where ROWID=" + String.valueOf(rowId), null);
//        c.moveToFirst();
//
//        temp.setId(c.getLong(0));
//        c.close();
        //close();


        return temp;
    }

    public List<Temperature> insertTemperatureMany(List<Temperature> temperatureList) {
        database.beginTransaction();
        for (Temperature temp :
                temperatureList) {
            temp = insertTemperature(temp);
        }
        database.endTransaction();
        return temperatureList;
    }

    public List<Temperature> insertTemperatureMany(List<Temperature> temperatureList, MainActivity c) {
        long startMillis = System.currentTimeMillis();
        database.beginTransaction();
        int count = 0;
        String sql = "Insert into " + SQLiteDBHelper.TABLE_Temp
                + " (" + SQLiteDBHelper.COLUMN_TEMP + "," + SQLiteDBHelper.COLUMN_Date + "," + SQLiteDBHelper.COLUMN_SENSOR + ")"
                + "VALUES (?,?,?)";
        System.out.println(sql);
        statement = database.compileStatement(sql);
        for (Temperature temp :
                temperatureList) {
            temp = insertTemperatureWithPreparedStatement(temp);
            count++;
            if (count % 100 == 0)
                c.updateProgress(temperatureList.size(), count);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        database.rawQuery("Create Index 'temperature_date_index' ON '" + SQLiteDBHelper.TABLE_Temp + "' ('" + SQLiteDBHelper.COLUMN_Date + "')", null);
        long endMillis = System.currentTimeMillis();

        System.out.println((endMillis - startMillis));

        return temperatureList;

    }

    public List<Temperature> getAllTemperatures() {
        List<Temperature> temps = new LinkedList<>();

        if (countEntries() == 0)
            return temps;

        //open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp + " ORDER BY " + SQLiteDBHelper.COLUMN_Date, null);

        c.moveToFirst();
        Temperature t;
        while (!c.isAfterLast()) {

            t = new Temperature(c.getDouble(2), new java.util.Date(c.getLong(1)), c.getLong(0));
            if (c.getString(3) == null) {
                t.setSensorID("");
            }
            t.setSensorID(c.getString(3));
            temps.add(t);
            c.moveToNext();
        }
        //t = new Temperature(c.getDouble(2), new java.util.Date(c.getLong(1)), c.getLong(0));
        //temps.add(t);
        //close();
        return temps;
    }

    public List<java.util.Date> getAllPossibleDates(String sensor) {


        return generateDatebetween(getMinDate(sensor), getMaxDate(sensor));
    }

    public void clearTable() {
        open();
        database.execSQL("Delete from " + SQLiteDBHelper.TABLE_Temp);
        //close();
    }

    public List<Temperature> getTempsBetween(java.util.Date startDate, java.util.Date endDate, String sensorID) {
        return getTempsBetweenNew(startDate, endDate, sensorID);
//        List<Temperature> result = new LinkedList<>();
//        if (startDate == null || endDate == null)
//            return result;
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//        long s = startDate.getTime();
//        long en = endDate.getTime();
//        long d;
//
//        for (Temperature t :
//                getAllTemperatures()) {
//            try {
//                java.util.Date date2 = sdf.parse((String) android.text.format.DateFormat.format("dd.MM.yyyy", t.getTime()));
//                java.util.Date date = t.getTime();
//                d = date.getTime();
//                if (date.getTime() >= startDate.getTime() && date.getTime() <= endDate.getTime()) {
//                    result.add(t);
//                }
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//
//
//        }
//
//        Temperature temp1;
//        Temperature temp2;
//        List<Temperature> minimizedList;
//        int numberPoints = Settings.getInstance().getPoolSettings().getNumberOfPoints();
//        while (result.size() >= numberPoints) {
//            minimizedList = new LinkedList<>();
//            for (int i = 0; i < result.size(); i = i + 2) {
//                if (i + 1 < result.size()) {
//                    temp1 = result.get(i);
//                    temp2 = result.get(i + 1);
//                    temp1.setTemperature((temp1.getTemperature() + temp2.getTemperature()) / 2);
//                    minimizedList.add(temp1);
//                }
//            }
///*
//            temp1=result.get(0);
//            temp2=result.get(1);
//            result.remove(0);
//            result.remove(0);
//            temp1.setTemperature((temp1.getTemperature()+temp2.getTemperature())/2);
//            minimizedList.add(temp1);
//            */
//            result = minimizedList;
//        }
//
//        for (Temperature t :
//                result) {
//            t.setTemperature(round(t.getTemperature(), 2));
//
//        }
//
//        return result;
    }

    public List<Temperature> getTempsBetweenNew(Date startDate, Date endDate, String sensorID) {
        List<Temperature> temps = new LinkedList<>();

        if (countEntries() == 0)
            return temps;

        //open();
//        System.out.println(startDate.getTime()+"    "+endDate.getTime());
        String query;
        if (sensorID == null || sensorID == "") {
            query = "Select * from " + SQLiteDBHelper.TABLE_Temp
                    + " where " + SQLiteDBHelper.COLUMN_Date + "   BETWEEN " + startDate.getTime() + " AND " + "" + endDate.getTime()
                    + " ORDER BY " + SQLiteDBHelper.COLUMN_Date;
        } else {
            query = "Select * from " + SQLiteDBHelper.TABLE_Temp
                    + " where " + SQLiteDBHelper.COLUMN_Date + "   BETWEEN " + startDate.getTime() + " AND " + "" + endDate.getTime() + " AND " + SQLiteDBHelper.COLUMN_SENSOR + " LIKE '" + sensorID+"'"
                    + " ORDER BY " + SQLiteDBHelper.COLUMN_Date;
        }
        System.out.println(query);

        Cursor c = database.rawQuery(query, null);


        c.moveToFirst();
        Temperature t;

        while (!c.isAfterLast()) {
//            System.out.println(c.getDouble(2)+"     "+c.getDouble(1)+"     "+c.getDouble(0)+"     ");
            t = new Temperature(c.getDouble(2), new java.util.Date(c.getLong(1)), c.getLong(0));
            t.setSensorID(c.getString(3));
            temps.add(t);
            c.moveToNext();
//            System.out.println(t.getTime());
        }
//        System.out.println(temps.size());
        //close();
        return minimizeList(temps);
    }

    public int countEntries() {
        open();
        Cursor c = database.rawQuery("Select count(*) from " + SQLiteDBHelper.TABLE_Temp, null);
        c.moveToFirst();
        int ret = c.getInt(0);
        //close();
        return ret;
    }

    public Temperature getHighestTemperature(String sensor) {

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp+ " where "+SQLiteDBHelper.COLUMN_SENSOR + " = '"+sensor+"'" + " ORDER BY " + SQLiteDBHelper.COLUMN_TEMP + " DESC", null);

        c.moveToFirst();
        if (!c.isAfterLast()) {
            Temperature t = new Temperature(round(c.getDouble(2), 1), new java.util.Date(c.getLong(1)), c.getLong(0));
            //close();
            return t;
        }
        //close();
        return null;

    }

    public Temperature getLowestTemperature(String sensor) {

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp+ " where "+SQLiteDBHelper.COLUMN_SENSOR + " = '"+sensor+"'" + " ORDER BY " + SQLiteDBHelper.COLUMN_TEMP, null);

        c.moveToFirst();

        Temperature t = new Temperature(round(c.getDouble(2), 1), new java.util.Date(c.getLong(1)), c.getLong(0));

        //close();
        return t;

    }

    public Temperature getActualTemperature(String sensor) {

        open();
        Cursor c = database.rawQuery("Select * from " + SQLiteDBHelper.TABLE_Temp+ " where "+SQLiteDBHelper.COLUMN_SENSOR + " = '"+sensor+"'" + " ORDER BY " + SQLiteDBHelper.COLUMN_Date + " DESC", null);

        c.moveToFirst();
        Temperature t;
        try {
            t = new Temperature(round(c.getDouble(2), 1), new java.util.Date(c.getLong(1)), c.getLong(0));

        } catch (Exception e) {
            t = new Temperature(0, new java.util.Date(0));
        }

        //close();
        return t;

    }

    public int getDateRange(String sensor) {

        List<java.util.Date> dates = getAllPossibleDates(sensor);

        return dates.size() - 1;
        /*
        if(dates.size()==1)
            return 0;
        else
        {
            long diff=Math.abs(dates.get(0).getTime()-dates.get(dates.size()-1).getTime());

            return (int)(diff / (24 * 60 * 60 * 1000));
        }*/

    }

    public double getAverageOfYesterday(String sensor) {

        open();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long startDate = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        long endDate = calendar.getTimeInMillis();

        String query = "Select AVG(" + SQLiteDBHelper.COLUMN_TEMP + ") from " + SQLiteDBHelper.TABLE_Temp +
                " where " + SQLiteDBHelper.COLUMN_Date + " >= " + startDate + " and " + SQLiteDBHelper.COLUMN_Date + " <= " + endDate+ " and "+SQLiteDBHelper.COLUMN_SENSOR + " = '"+sensor+"'";

        Cursor c = database.rawQuery(query, null);

        c.moveToFirst();
        double result = c.getDouble(0);
        //close();
        return round(result, 1);
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public java.util.Date getMinDate(String sensor) {
        if (countEntries() == 0)
            return new java.util.Date(Long.MIN_VALUE);

        open();
        Cursor c = database.rawQuery("Select Min(" + SQLiteDBHelper.COLUMN_Date + ") from " + SQLiteDBHelper.TABLE_Temp+ " where "+SQLiteDBHelper.COLUMN_SENSOR + " = '"+sensor+"'", null);

        c.moveToFirst();

        long ret = c.getLong(0);
        //close();
        c.close();
        return new java.util.Date(ret);
    }

    public java.util.Date getMaxDate(String sensor) {
        if (countEntries() == 0)
            return new java.util.Date(Long.MIN_VALUE);

        open();
        Cursor c = database.rawQuery("Select Max(" + SQLiteDBHelper.COLUMN_Date + ") from " + SQLiteDBHelper.TABLE_Temp+ " where "+SQLiteDBHelper.COLUMN_SENSOR + " = '"+sensor+"'", null);

        c.moveToFirst();
        long ret = c.getLong(0);
        //close();
        return new java.util.Date(ret);
    }

    public List<java.util.Date> generateDatebetween(java.util.Date minDate, java.util.Date maxDate) {
        List<java.util.Date> dateList = new LinkedList<>();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(minDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        dateList.add(calendar.getTime());

        while (calendar.getTime().getTime() < maxDate.getTime()) {
            calendar.add(Calendar.DATE, 1);
            dateList.add(calendar.getTime());
        }
        dateList.remove(dateList.size() - 1);
        return dateList;
    }

    private List<Temperature> minimizeList(List<Temperature> fullList) {
        Temperature temp1;
        Temperature temp2;
        List<Temperature> minimizedList = new LinkedList<>();
        int numberPoints = Settings.getInstance().getPoolSettings().getNumberOfPoints();
        while (fullList.size() >= numberPoints) {
            minimizedList = new LinkedList<>();
            for (int i = 0; i < fullList.size(); i = i + 2) {
                if (i + 1 < fullList.size()) {
                    temp1 = fullList.get(i);
                    temp2 = fullList.get(i + 1);
                    temp1.setTemperature((temp1.getTemperature() + temp2.getTemperature()) / 2);
                    minimizedList.add(temp1);
                }
            }
            fullList = minimizedList;
/*
            temp1=result.get(0);
            temp2=result.get(1);
            result.remove(0);
            result.remove(0);
            temp1.setTemperature((temp1.getTemperature()+temp2.getTemperature())/2);
            minimizedList.add(temp1);
            */
        }
        if (minimizedList.size() == 0)
            return fullList;
        return minimizedList;
    }
}
