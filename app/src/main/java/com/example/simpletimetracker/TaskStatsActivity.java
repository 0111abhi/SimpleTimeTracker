package com.example.simpletimetracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.material.resources.TextAppearance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskStatsActivity extends AppCompatActivity {
    private TaskDataBaseHandler db;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_stats);
        db = new TaskDataBaseHandler(this);
        Bundle b = getIntent().getExtras();
        if(b.getString("statsType").matches("recent"))
            recentStats();
        else
            weeklyStats();
    }

    public void recentStats()
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        String[] projection = {TasksDataContract.TasksEntry.KEY_STARTTIME, TasksDataContract.TasksEntry.KEY_ENDTIME, TasksDataContract.TasksEntry.KEY_TASK};
        Cursor cursor = reader.rawQuery("SELECT strftime('%m/%d', start_time) AS Date, strftime('%H : %M', start_time) AS StartTime, strftime('%H : %M', end_time) AS end_time, category, task FROM TasksTime WHERE start_time > date('now', '-1 day', 'localtime') ORDER BY start_time DESC", new String[] {});

        TableLayout table = DisplayTable(cursor, new String[] {"Date", "StartTime", "EndTime", "Category", "Task"});
        LinearLayout mainLayout = this.findViewById(R.id.taskStats);
        mainLayout.addView(table);

    }
    public void weeklyStats()
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        String[] projection = {TasksDataContract.TasksEntry.KEY_STARTTIME, TasksDataContract.TasksEntry.KEY_ENDTIME, TasksDataContract.TasksEntry.KEY_TASK};
        Cursor cursor = reader.rawQuery("SELECT category, ROUND(SUM(strftime(\"%s\", end_time) - strftime(\"%s\", start_time))/3600.0, 2) AS HoursSpent FROM TasksTime WHERE start_time > date('now', '-7 day', 'localtime') GROUP BY LOWER(category) ORDER BY HoursSpent DESC", new String[] {});
        DisplayWeeklyStatusTable(cursor, new String[] {"Category", "Total Hours"});
    }

    TableLayout DisplayTable(Cursor cursor, String[] headers)
    {
        if(cursor != null)
            cursor.moveToFirst();
        TableLayout table = new TableLayout(this);
        TableRow headersRow = new TableRow(this);
        //headersRow.setBackgroundColor(android.graphics.Color.rgb(244,149,0));

        headersRow.setPadding(10,10,10,10);
        for(String header:headers)
        {
            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setPadding(0,10,20,10);
            headersRow.addView(tv);
        }

        table.addView(headersRow);
        while(!cursor.isAfterLast()) {
            TableRow contentRow = new TableRow(this);
            contentRow.setPadding(10,10,10,10);
            //contentRow.setBackgroundColor(android.graphics.Color.rgb(225,232,250));

            for(int i = 0; i<headers.length; i++)
            {
                TextView tv = new TextView(this);
                tv.setText(cursor.getString(i));
                tv.setPadding(0,10,20,10);
                contentRow.addView(tv);
            }

            table.addView(contentRow);
            cursor.moveToNext();
        }
        return table;
    }

    void DisplayWeeklyStatusTable(Cursor cursor, String[] headers)
    {
        if(cursor != null)
            cursor.moveToFirst();

        TableLayout mainTable = new TableLayout(this);

        // Set main table view properties
        mainTable.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
        mainTable.setColumnStretchable(0, true);
        mainTable.setColumnStretchable(1, true);

        // Create header row for main table
        TableRow headerRow = new TableRow(this);

        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
        headerRow.setBackgroundColor(android.graphics.Color.rgb(255,149,0));
        headerRow.setPadding(10,10,10,10);
        for(int i = 0; i < headers.length; i++)
        {
            String header = headers[i];
            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setPadding(0,10,20,10);
            tv.setTypeface(null, Typeface.BOLD);
            headerRow.addView(tv);
        }
        mainTable.addView(headerRow);

        // Category data row for main table
        while(!cursor.isAfterLast()) {
            TableRow categoryDataRow = new TableRow(this);

            categoryDataRow.setPadding(10,10,10,10);

            // add category at column = 0
            TextView category = new TextView(this);
            category.setText(cursor.getString(0));
            category.setPadding(0,10,20,10);
            categoryDataRow.addView(category);

            // add hours spent to column = 1
            TextView hoursSpent = new TextView(this);
            hoursSpent.setText(cursor.getString(1));
            hoursSpent.setPadding(0,10,20,10);
            categoryDataRow.addView(hoursSpent);
            categoryDataRow.setBackgroundColor(android.graphics.Color.rgb(225,232,250));

            ToggleButton toggleButton = new ToggleButton(this);
            toggleButton.setTextSize(20);
            toggleButton.setText("+");
            toggleButton.setTextOff("+");
            toggleButton.setTextOn("-");
            toggleButton.layout(10,10,10,10);
            categoryDataRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
            toggleButton.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));
            categoryDataRow.addView(toggleButton);

            TableLayout taskDrillDownTable = CreateSubTable(cursor.getString(0), new String[]{"Task", "Hours Spent"});
            taskDrillDownTable.setVisibility(View.GONE);
            taskDrillDownTable.setPadding(0,0,0,0);

            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(toggleButton.isChecked()) {
                        taskDrillDownTable.setVisibility(View.VISIBLE);
                        TableRow.LayoutParams params = (TableRow.LayoutParams)taskDrillDownTable.getLayoutParams();
                        params.span = 3;
                        taskDrillDownTable.setLayoutParams(params);

                    }
                    else
                        taskDrillDownTable.setVisibility(View.GONE);
                }
            });

            mainTable.addView(categoryDataRow);

            TableRow taskDrillDownRow = new TableRow(this);
            taskDrillDownRow.addView(taskDrillDownTable);

            mainTable.addView(taskDrillDownRow);
            cursor.moveToNext();
        }
        LinearLayout mainLayout = this.findViewById(R.id.taskStats);
        mainLayout.addView(mainTable);
    }

    private TableLayout CreateSubTable(String category, String[] headers)
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        Cursor cursor = reader.rawQuery("SELECT task, ROUND(SUM(strftime(\"%s\", end_time) - strftime(\"%s\", start_time))/3600.0, 2) AS HoursSpent FROM TasksTime WHERE start_time > date('now', '-7 day') AND LOWER(category) = LOWER(?) GROUP BY LOWER(task) ORDER BY HoursSpent DESC", new String[] {category});
        return DisplayTable(cursor, headers);
    }



}