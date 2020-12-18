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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
        Cursor cursor = reader.rawQuery("SELECT strftime('%m/%d', start_time) AS Date, strftime('%H : %M', start_time) AS StartTime, strftime('%H : %M', end_time) AS end_time, task FROM TasksTime WHERE start_time > date('now', '-1 day') ORDER BY start_time DESC", new String[] {});

        DisplayTable(cursor, new String[] {"Date", "StartTime", "EndTime", "Task"}, 4);

    }
    public void weeklyStats()
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        String[] projection = {TasksDataContract.TasksEntry.KEY_STARTTIME, TasksDataContract.TasksEntry.KEY_ENDTIME, TasksDataContract.TasksEntry.KEY_TASK};
        Cursor cursor = reader.rawQuery("SELECT task, SUM(strftime(\"%s\", end_time) - strftime(\"%s\", start_time))/60 AS MinutesSpent, SUM(strftime(\"%s\", end_time) - strftime(\"%s\", start_time))/3600.0 AS HoursSpent FROM TasksTime WHERE start_time > date('now', '-7 day') GROUP BY LOWER(task) ORDER BY MinutesSpent DESC", new String[] {});
        DisplayTable(cursor, new String[] {"Task", "MinutesSpent", "HoursSpent"}, 3);
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

}