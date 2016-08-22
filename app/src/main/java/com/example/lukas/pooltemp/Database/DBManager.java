package com.example.lukas.pooltemp.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lukas on 14.08.2016.
 */
public class DBManager {

    private static DBManager instance;
    private SQLiteDatabase db;
    private Context context;
    public static DBManager getInstance(Context context){
        if(instance==null){
            instance= new DBManager(context);
        }
        return instance;
    }
    private DBManager(Context context){
        this.context=context;
    }

    public void openOrCreateDatabase(String dbName){

        db= android.database.sqlite.SQLiteDatabase.openOrCreateDatabase("/data/data/at.wickenhauser.lukas/databases/poolTemp.db", null);
        db.beginTransaction();
        db.execSQL("Create Table If Not Exists Temp(Time Date, Temperature DOUBLE)");
        db.execSQL("Insert Into Temp values('2016-10-11 12:00:00.000)");
        db.endTransaction();
    }

    public List<Double> getTemp(){
        List<Double> temps=new LinkedList<>();
        db.beginTransaction();
        Cursor c =db.rawQuery("Select * from Temp", null);

        c.moveToFirst();
        temps.add(c.getDouble(1));

        while(!c.isLast())
        {
            c.moveToNext();
            temps.add(c.getDouble(1));
        }


        return temps;
    }

}
