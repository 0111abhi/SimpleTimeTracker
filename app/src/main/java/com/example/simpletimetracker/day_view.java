package com.example.simpletimetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class day_view extends AppCompatActivity {

    private TaskDataBaseHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view3);
        LocalDateTime now = LocalDateTime.now();
        LinearLayout layout = this.findViewById(R.id.dayView);
        layout.addView(createViewForDay(now));
    }

    private TableLayout createViewForDay(LocalDateTime date)
    {
        db = new TaskDataBaseHandler(this);
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setColumnStretchable(1,true);
        tableLayout.setColumnStretchable(2,true);

        SQLiteDatabase reader = db.getReadableDatabase();
        Cursor cursor = reader.rawQuery(ResourceUrl.READ_TODAY_ROWS_SQL,new String[]{});

        if (cursor != null)
            cursor.moveToFirst();
        int previousEndTime = 0;
        ArrayList<Task> taskList = new ArrayList<Task>();
        while (!cursor.isAfterLast()){
            int startTimeInMinutes = ResourceUrl.GetTotalMinutes(cursor.getString(0), cursor.getString(1));
            startTimeInMinutes = RoundToNearest15Minute(startTimeInMinutes);
            int endTimeInMinutes = ResourceUrl.GetTotalMinutes(cursor.getString(2), cursor.getString(3));
            endTimeInMinutes = RoundToNearest15Minute(endTimeInMinutes);
            String taskDescription = cursor.getString(4);
            String category = cursor.getString(5);
            if(previousEndTime<startTimeInMinutes) {
                taskList.add(new Task(previousEndTime, startTimeInMinutes, "Not Entered", "NA"));
            }
            else if(previousEndTime>startTimeInMinutes){
                continue;
            }
            previousEndTime = endTimeInMinutes;
            taskList.add(new Task(startTimeInMinutes, endTimeInMinutes, taskDescription, category));

            cursor.moveToNext();
        }

        createViewFromTaskList(taskList, tableLayout);
        return tableLayout;
    }

    private void createViewFromTaskList(ArrayList<Task> taskList, TableLayout tableLayout) {
        for ( Task task : taskList ){
            TableRow row = new TableRow(this);

            int height = calculateHeight(task.startTime, task.endTime);
            TableRow.LayoutParams params = new TableRow.LayoutParams(50,height);
            //row.setLayoutParams(params);
            String time = formatTime(task.startTime, task.endTime);
            addValuesInRow(row, time,100, height);
            addValuesInRow(row, task.taskDescription,200, height);
            addValuesInRow(row, task.category,200, height);
            tableLayout.addView(row);
        }
    }

    private String formatTime(int startTime, int endTime) {
        String tempStartTime = convertToString(startTime);
        String tempEndTime = convertToString(endTime);

        return tempStartTime +" - "+tempEndTime;
    }

    private String convertToString(int time){
        int hours = time/60;
        int minutes = time%60;
        String tempHour=String.valueOf(hours);
        String tempMin=String.valueOf(minutes);
        if (hours <10){
            tempHour = '0'+tempHour;
        }
        if (minutes<10){
            tempMin ='0'+ tempMin;
        }
        return tempHour+":"+tempMin;
    }

    private int calculateHeight(int startTime, int endTime) {
        int diff = endTime-startTime;
        if(diff > 120){
            diff = 120;
        }
        return diff*4;
    }

    private void addValuesInRow(TableRow row, String val, int width, int height){
        TextView tv = new TextView(this);
        tv.setText(val);
        Random rand = new Random();
        row.setBackgroundColor(android.graphics.Color.rgb(150 + rand.nextInt() % 100,150 + rand.nextInt() % 100,230));
        row.addView(tv, new TableRow.LayoutParams(-1,height));
    }


    protected int RoundToNearest15Minute(int minutes)
    {
        int offset = minutes % 15;
        if(offset < 8)
            return minutes - offset;
        return minutes + (15 - offset);
    }
}