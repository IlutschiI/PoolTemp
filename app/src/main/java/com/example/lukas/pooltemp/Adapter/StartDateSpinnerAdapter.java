package com.example.lukas.pooltemp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.lukas.pooltemp.R;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

/**
 * Created by wicki on 22.08.2016.
 */
public class StartDateSpinnerAdapter extends ArrayAdapter {

    private Context context;
    private List<Date> data;
    private LayoutInflater inflater;
    private Date minDate;



    public StartDateSpinnerAdapter(Context context, List<Date> data) {
        super(context, android.R.layout.simple_spinner_item,data);
        inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.data=data;
        this.context=context;
        data.add(0,new Date(Long.MIN_VALUE));
        minDate=new Date(Long.MAX_VALUE);
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row=inflater.inflate(R.layout.spinner_item,parent,false);

        TextView textView=(TextView)row.findViewById(R.id.spinnerItem);
        Date currentDate=data.get(position);
        int year = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", currentDate));
        int month = Integer.valueOf((String) android.text.format.DateFormat.format("MM", currentDate));
        int day = Integer.valueOf((String) android.text.format.DateFormat.format("dd", currentDate));

        if(position!=0)
        textView.setText("" + day + "." + month + "." + year);
        else
            textView.setText("Datum auswählen");


        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {


        View row =  inflater.inflate(R.layout.spinner_dropdown_item,parent,false);
        TextView textView=(TextView) row.findViewById(R.id.spinnerDropDownItem);
        Date currentDate=data.get(position);
        int year = Integer.valueOf((String) android.text.format.DateFormat.format("yyyy", currentDate));
        int month = Integer.valueOf((String) android.text.format.DateFormat.format("MM", currentDate));
        int day = Integer.valueOf((String) android.text.format.DateFormat.format("dd", currentDate));

        if(position==0)
        {
            textView.setVisibility(View.GONE);
            row.setVisibility(View.GONE);
        }
        else
        textView.setText("" + day + "." + month + "." + year);

        if(!(minDate.getTime()>=currentDate.getTime()))
            return new View(parent.getContext());

        return row;
    }
}
