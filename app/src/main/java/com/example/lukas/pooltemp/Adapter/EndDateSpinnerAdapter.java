package com.example.lukas.pooltemp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.lukas.pooltemp.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by wicki on 22.08.2016.
 */
public class EndDateSpinnerAdapter extends ArrayAdapter {

    private Context context;
    private List<Date> data;
    private LayoutInflater inflater;
    private Date minDate;



    public EndDateSpinnerAdapter(Context context, List<Date> data) {
        super(context, android.R.layout.simple_spinner_item,data);
        inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.data=data;
        this.context=context;
        data.add(0,new Date(Long.MIN_VALUE));
        minDate=new Date(Long.MIN_VALUE);
    }

    public void setMinDate(Date minDate) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(minDate);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.MINUTE,0);
        this.minDate = cal.getTime();
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
        if((minDate.getTime()>=currentDate.getTime())){
            return new View(parent.getContext());
        }


        return row;
    }

    public void setData(List<Date> data) {
        data.add(0,new Date(Long.MAX_VALUE));
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }
}
