package com.example.simpletimetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class day_view_input extends AppCompatActivity {

    private TaskDataBaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view_input);
        LinearLayout layout = this.findViewById(R.id.dayViewInput);
        layout.addView(createViewForDay());
    }

    private TableLayout createViewForDay()
    {
        db = new TaskDataBaseHandler(this);
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setColumnStretchable(2,true);
        tableLayout.setColumnStretchable(3,true);

        int start = 0;
        for( int i = 0; i < 24 * 2; i++) {
            TableRow row = new TableRow(this);

            TextView tv = new TextView(this);
            tv.setText(String.valueOf(start));
            row.addView((tv));

            TextView tv2 = new TextView(this);
            tv2.setText(String.valueOf(start + 30));
            row.addView((tv2));

            Button button = new Button(this);
            button.setText("NA");
            //button.setId(start);
            start = start + 30;
            row.addView(button);

            tableLayout.addView(row);
        }
        return tableLayout;
    }
}