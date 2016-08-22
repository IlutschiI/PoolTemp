package com.example.lukas.pooltemp.Initialize;

import com.example.lukas.pooltemp.Database.TemperatureDataSource;
import com.example.lukas.pooltemp.Model.Temperature;

import java.util.Date;

/**
 * Created by Lukas on 22.08.2016.
 */
public class Init {


    public static void initDB(TemperatureDataSource TempSource){
        TempSource.open();
        TempSource.clearTable();
        TempSource.insertTemperature(new Temperature(20.2, new Date(System.currentTimeMillis())));
        TempSource.insertTemperature(new Temperature(21.2, new Date(System.currentTimeMillis()+86400000)));
        TempSource.insertTemperature(new Temperature(22.2,new Date(System.currentTimeMillis()+2*86400000)));
        TempSource.insertTemperature(new Temperature(20.9,new Date(System.currentTimeMillis()+3*86400000)));
        TempSource.insertTemperature(new Temperature(21.4, new Date(System.currentTimeMillis()+4*86400000)));
        TempSource.insertTemperature(new Temperature(22.4, new Date(System.currentTimeMillis()+5*86400000)));
        TempSource.insertTemperature(new Temperature(23.4, new Date(System.currentTimeMillis()+6*86400000)));
        TempSource.insertTemperature(new Temperature(20.4, new Date(System.currentTimeMillis()+7*86400000)));
        TempSource.insertTemperature(new Temperature(18.4, new Date(System.currentTimeMillis()+8*86400000)));
        TempSource.insertTemperature(new Temperature(15.4, new Date(System.currentTimeMillis()+9*86400000)));
        TempSource.insertTemperature(new Temperature(21.4, new Date(System.currentTimeMillis()+10*86400000)));


    }

}
