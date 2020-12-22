package com.example.simpletimetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.Date;

public class day_view extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view3);
    }

    private void createViewForDay(Date date)
    {
       // LinearLayout layout = new LinearLayout();
       // addDayItems(layout);
    }

    protected void addDayItems(LinearLayout layout)
    {
        //Cursor cursor = GetDataForDay();

    }
}