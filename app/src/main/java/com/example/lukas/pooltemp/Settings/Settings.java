package com.example.lukas.pooltemp.Settings;

/**
 * Created by wicki on 24.09.2016.
 */

public class Settings{

    private static Settings instance;

    public static Settings getInstance(){
        if(instance==null)
            instance=new Settings();
        return instance;
    }




    private PoolSettings poolSettings;

    private Settings() {
        poolSettings=new PoolSettings();
    }

    private Settings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }

    public PoolSettings getPoolSettings() {
        return poolSettings;
    }

    public void setPoolSettings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }
}
