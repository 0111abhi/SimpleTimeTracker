package com.example.simpletimetracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

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
        Cursor cursor = reader.rawQuery("SELECT strftime('%m/%d', start_time) AS Date, strftime('%H : %M', start_time) AS StartTime, strftime('%H : %M', end_time) AS end_time, category, task FROM TasksTime WHERE start_time > date('now', '-1 day') ORDER BY start_time DESC", new String[] {});

        DisplayTable(cursor, new String[] {"Date", "StartTime", "EndTime", "Category", "Task"}, 5);

    }
    public void weeklyStats()
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        String[] projection = {TasksDataContract.TasksEntry.KEY_STARTTIME, TasksDataContract.TasksEntry.KEY_ENDTIME, TasksDataContract.TasksEntry.KEY_TASK};
        Cursor cursor = reader.rawQuery("SELECT category, ROUND(SUM(strftime(\"%s\", end_time) - strftime(\"%s\", start_time))/3600.0, 2) AS HoursSpent FROM TasksTime WHERE start_time > date('now', '-7 day') GROUP BY LOWER(category) ORDER BY HoursSpent DESC", new String[] {});
        DisplayWeeklyStatusTable(cursor, new String[] {"Category", "HoursSpent"}, 2);
    }

    void DisplayTable(Cursor cursor, String[] headers, int columns)
    {
        if(cursor != null)
            cursor.moveToFirst();
        TableLayout table = new TableLayout(this);
        TableRow row = new TableRow(this);
        row.setPadding(10,10,10,10);
        for(String header:headers)
        {
            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setPadding(0,10,20,10);
            row.addView(tv);
        }

        table.addView(row);
        while(!cursor.isAfterLast()) {
            row = new TableRow(this);
            row.setPadding(10,10,10,10);
            for(int i = 0; i<columns; i++)
            {
                TextView tv = new TextView(this);
                tv.setText(cursor.getString(i));
                tv.setPadding(0,10,20,10);
                row.addView(tv);
            }

            table.addView(row);
            cursor.moveToNext();
        }
        LinearLayout mainLayout = this.findViewById(R.id.taskStats);
        mainLayout.addView(table);
    }

    void DisplayWeeklyStatusTable(Cursor cursor, String[] headers, int columns)
    {
        if(cursor != null)
            cursor.moveToFirst();
        TableLayout table = new TableLayout(this);
        TableRow row = new TableRow(this);
        row.setPadding(10,10,10,10);
        for(int i = 0; i < headers.length; i++)
        {
            String header = headers[i];
            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setPadding(0,10,20,10);
            tv.setLayoutParams(new TableRow.LayoutParams(1));
            row.addView(tv);
        }

        table.addView(row);
        while(!cursor.isAfterLast()) {
            row = new TableRow(this);
            row.setPadding(10,10,10,10);
            ToggleButton toggleButton = new ToggleButton(this);
            row.addView(toggleButton);

            // add category to column = 1
            TextView tv = new TextView(this);
            tv.setText(cursor.getString(0));
            tv.setPadding(0,10,20,10);
            row.addView(tv);

            // add hours spent and table to column = 2
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            TextView hoursSpent = new TextView(this);
            hoursSpent.setText(cursor.getString(1));
            hoursSpent.setPadding(0,10,20,10);
            linearLayout.addView(hoursSpent);
            TableLayout tasksTable = CreateSubTable(cursor.getString(0), new String[]{"Task", "HoursSpent"});
            tasksTable.setVisibility(View.GONE);
            linearLayout.addView(tasksTable);
            row.addView(linearLayout);

            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(toggleButton.isChecked())
                        tasksTable.setVisibility(View.VISIBLE);
                    else
                        tasksTable.setVisibility(View.GONE);
                }
            });


            table.addView(row);
            cursor.moveToNext();
        }
        LinearLayout mainLayout = this.findViewById(R.id.taskStats);
        mainLayout.addView(table);
    }

    private TableLayout CreateSubTable(String category, String[] headers)
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        Cursor cursor = reader.rawQuery("SELECT task, ROUND(SUM(strftime(\"%s\", end_time) - strftime(\"%s\", start_time))/3600.0, 2) AS HoursSpent FROM TasksTime WHERE start_time > date('now', '-7 day') AND category = ? GROUP BY LOWER(task) ORDER BY HoursSpent DESC", new String[] {category});
        TableLayout table = new TableLayout(this);
        TableRow row = new TableRow(this);
        row.setPadding(10,10,10,10);
        for(int i = 0; i < headers.length; i++)
        {
            String header = headers[i];
            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setPadding(0,10,20,10);
            row.addView(tv);
        }

        if(cursor != null)
            cursor.moveToFirst();

        table.addView(row);
        while(!cursor.isAfterLast()) {
            row = new TableRow(this);
            row.setPadding(10,10,10,10);

            for(int i = 0; i<headers.length; i++)
            {
                TextView tv = new TextView(this);
                tv.setText(cursor.getString(i));
                tv.setPadding(0,10,20,10);
                row.addView(tv);
            }

            table.addView(row);
            cursor.moveToNext();
        }
        return table;
    }

}