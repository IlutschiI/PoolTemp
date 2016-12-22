package com.example.lukas.pooltemp.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lukas on 14.08.2016.
 */
public class SQLiteDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_Temp = "temperature";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_Date = "time";
    public static final String COLUMN_TEMP="temp";


    private static final String DATABASE_NAME = "temp.db";
    private static final int DATABASE_VERSION = 4;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_Temp + "( " + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_Date + " Long not null, "
            +COLUMN_TEMP+" Double not null);";


    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop Table If Exists "+TABLE_Temp);
        onCreate(db);
    }
}
