package com.example.simpletimetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    private TaskDataBaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new TaskDataBaseHandler(this);
    }

    public void saveData(View view){
        EditText inputTask = (EditText)findViewById(R.id.tasksInput);
        String task = inputTask.getText().toString();

        EditText inputCategory = (EditText)findViewById(R.id.categoryInput);
        String category = inputCategory.getText().toString();

        TimePicker startTime =(TimePicker)findViewById(R.id.startTime);
        TimePicker endTime = (TimePicker)findViewById(R.id.endTime);
        boolean isValid = CheckForValidtiy(task, startTime, endTime);
        if(!isValid)
            return;
        int startHour = startTime.getHour();
        int startMin = startTime.getMinute();

        int endHour = endTime.getHour();
        int endMin = endTime.getMinute();



        LocalDateTime now = LocalDateTime.now();
        int todayHour = now.getHour();
        int todayMins = now.getMinute();
        now = now.minusHours(todayHour).minusMinutes(todayMins);
        LocalDateTime startDate = now.plusHours(startHour).plusMinutes(startMin);
        LocalDateTime endDate = now.plusHours(endHour).plusMinutes(endMin);

        SQLiteDatabase dbWrite = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TasksDataContract.TasksEntry.KEY_STARTTIME, startDate.toString());
        values.put(TasksDataContract.TasksEntry.KEY_ENDTIME, endDate.toString());
        values.put(TasksDataContract.TasksEntry.KEY_TASK, task);
        values.put(TasksDataContract.TasksEntry.KEY_CATEGORY, category);
        long newRowId = dbWrite.insert(TasksDataContract.TasksEntry.TABLE_NAME, null, values);
        inputTask.setText("");
        inputCategory.setText("");
    }

    private boolean CheckForValidtiy(String task, TimePicker startTime, TimePicker endTime)
    {
        if(task.matches("") || task == null)
            return false;

        int endTimeMinues = endTime.getHour() * 60 + endTime.getMinute();
        int startTimeMinutes = startTime.getHour() * 60 + startTime.getMinute();
        if(endTimeMinues <= startTimeMinutes)
            return false;

        // Check overlap
        if(CheckOverlapInDb(startTimeMinutes, endTimeMinues))
            return false;

        return true;
    }

    boolean CheckOverlapInDb(int newTimeStart, int newTimeEnd)
    {
        SQLiteDatabase reader = db.getReadableDatabase();
        // Get all task times for today
        Cursor cursor = reader.rawQuery("SELECT strftime('%H', start_time) AS StartTimeHour,strftime('%M', start_time) AS StartTimeMins, strftime('%H', end_time) AS EndTimeHour, strftime('%M', end_time) AS EndTimeMins FROM TasksTime WHERE date(start_time) == date('now') ORDER BY start_time DESC", new String[] {});
        if(cursor != null)
            cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            int existingTimeStart = parseInt(cursor.getString(0)) * 60 + parseInt(cursor.getString(1));
            int existingTimeEnd = parseInt(cursor.getString(2)) * 60 + parseInt(cursor.getString(3));
            if(CheckOverlapTime(existingTimeStart, existingTimeEnd, newTimeStart, newTimeEnd))
                return true;
            cursor.moveToNext();
        }
        return false;
    }

    boolean CheckOverlapTime(int existingTimeStart, int existingTimeEnd, int newTimeStart, int newTimeEnd)
    {
        if(existingTimeStart >= newTimeStart && existingTimeStart < newTimeEnd)
            return true;
        if(existingTimeEnd > newTimeStart && existingTimeEnd <= newTimeEnd)
            return true;

        if(newTimeStart >= existingTimeStart && newTimeStart < existingTimeEnd)
            return true;
        if(newTimeEnd > existingTimeStart && newTimeEnd <= existingTimeEnd)
            return true;

        return false;
    }

    public void recentStats(View view){
       // PopupStats popUp = new PopupStats();
       // popUp.showPopupWindow(view, db);
        Intent intent = new Intent(this, TaskStatsActivity.class);
        Bundle b = new Bundle();
        b.putString("statsType", "recent");
        intent.putExtras(b);
        startActivity(intent);
    }

    public void weeklyStats(View view){
        Intent intent = new Intent(this, TaskStatsActivity.class);
        Bundle b = new Bundle();
        b.putString("statsType", "weekly");
        intent.putExtras(b);
        startActivity(intent);
    }
}