package com.example.lukas.pooltemp.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lukas.pooltemp.R;

/**
 * Created by wicki on 18.11.2016.
 */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_layout,container,false);

        //TODO transfer Settings Logic from SettingsActivity

        return  view;
    }
}