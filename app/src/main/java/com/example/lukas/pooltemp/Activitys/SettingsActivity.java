package com.example.lukas.pooltemp.Activitys;

import android.content.SharedPreferences;
import android.os.Build;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lukas.pooltemp.R;
import com.example.lukas.pooltemp.Settings.PoolSettings;
import com.example.lukas.pooltemp.Settings.Settings;

/**
 * Created by wicki on 24.09.2016.
 */
public class SettingsActivity {

    private MainActivity activity;

    Switch swCubicCurve;
    Switch swXAxis;
    Switch swYAxis;
    SeekBar sbZoomMultiplier;
    TextView tvZoomMultiplier;
    Settings settings;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SettingsActivity(MainActivity activity) {
        this.activity = activity;
        settings=Settings.getInstance();
        sharedPreferences=activity.getSharedPreferences("settings", activity.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    public void initControls(){
        swCubicCurve=(Switch)activity.findViewById(R.id.swCubicCurve);
        swXAxis=(Switch)activity.findViewById(R.id.swXAxis);
        swYAxis=(Switch)activity.findViewById(R.id.swYAxis);
        sbZoomMultiplier=(SeekBar)activity.findViewById(R.id.sbZoomMultiplier);
        tvZoomMultiplier=(TextView)activity.findViewById(R.id.tvZoomMultiplier);
        sbZoomMultiplier.setMax(50);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            swCubicCurve.setChecked(settings.getPoolSettings().isCubicCurves());
            swXAxis.setChecked(settings.getPoolSettings().isxAxisEnabled());
            swYAxis.setChecked(settings.getPoolSettings().isyAxisEnabled());

            int progress=(int)(settings.getPoolSettings().getZoomingMultiplier()*10);
            //Toast.makeText(activity,progress+"",Toast.LENGTH_LONG).show();
            sbZoomMultiplier.setProgress(progress);
            tvZoomMultiplier.setText((double)progress/10+"x");

        }



        sbZoomMultiplier.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvZoomMultiplier.setText((double)seekBar.getProgress()/10+"x");
                editor.putFloat(PoolSettings.ZOOMING_MULTIPLIER,(float)seekBar.getProgress()/10);
                editor.commit();
                settings.getPoolSettings().setZoomingMultiplier((float)seekBar.getProgress()/10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        swCubicCurve.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(PoolSettings.CUBIC_CURVE,b);
                editor.commit();
                settings.getPoolSettings().setCubicCurves(b);
            }
        });
        swXAxis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(PoolSettings.XAXIS_ENABLED,b);
                editor.commit();
                settings.getPoolSettings().setxAxisEnabled(b);
            }
        });
        swYAxis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(PoolSettings.YAXIS_ENABLED,b);
                editor.commit();
                settings.getPoolSettings().setyAxisEnabled(b);
            }
        });




    }

    public void updateSettings(){
        settings.getPoolSettings().setCubicCurves(sharedPreferences.getBoolean(PoolSettings.CUBIC_CURVE,true));
        settings.getPoolSettings().setxAxisEnabled(sharedPreferences.getBoolean(PoolSettings.XAXIS_ENABLED,true));
        settings.getPoolSettings().setyAxisEnabled(sharedPreferences.getBoolean(PoolSettings.YAXIS_ENABLED,true));
        settings.getPoolSettings().setZoomingMultiplier(sharedPreferences.getFloat(PoolSettings.ZOOMING_MULTIPLIER,10));
    }
}
