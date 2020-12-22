package com.example.simpletimetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TimePicker;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
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

        AutoCompleteTextView inputTask = (AutoCompleteTextView)findViewById(R.id.tasksInput);
        ArrayAdapter<String> adapterTask = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getTaskSuggestion());
        inputTask.setAdapter(adapterTask);
        inputTask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                inputTask.showDropDown();
                return false;
            }
        });

        AutoCompleteTextView categoryInput = (AutoCompleteTextView)findViewById(R.id.categoryInput);
        categoryInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ArrayList<String> categorySuggestions = getCategorySuggestion(inputTask.getText().toString());
                if(categorySuggestions.size() > 0)
                    categoryInput.setText(categorySuggestions.get(0));
                ArrayAdapter<String> adapterTask = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1, categorySuggestions);
                categoryInput.setAdapter(adapterTask);
                categoryInput.showDropDown();
                return false;
            }
        });
    }


    private void PopulateAutoSuggestData(int input, ArrayList<String> suggestion)
    {
        AutoCompleteTextView inputTask = (AutoCompleteTextView)findViewById(input);
        ArrayAdapter<String> adapterTask = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, suggestion);
        inputTask.setAdapter(adapterTask);
        inputTask.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                inputTask.showDropDown();
                return false;
            }
        });
    }

    public void saveData(View view){
        AutoCompleteTextView inputTask = (AutoCompleteTextView)findViewById(R.id.tasksInput);
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
        Cursor cursor = reader.rawQuery("SELECT strftime('%H', start_time) AS StartTimeHour,strftime('%M', start_time) AS StartTimeMins, strftime('%H', end_time) AS EndTimeHour, strftime('%M', end_time) AS EndTimeMins FROM TasksTime WHERE date(start_time) > date('now', 'localtime') ORDER BY start_time DESC", new String[] {});
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

    public void dayView(View view){
        Intent intent = new Intent(this, day_view.class);
        Bundle b = new Bundle();
        b.putString("statsType", "weekly");
        intent.putExtras(b);
        startActivity(intent);
    }

    private ArrayList<String> getTaskSuggestion(){
        SQLiteDatabase reader = db.getReadableDatabase();
        // Get all task for today
        Cursor cursor = reader.rawQuery("select COUNT(task) AS TaskCnt, task from taskstime where date(end_time) > date('now', '-7 days') GROUP BY task ORDER BY TaskCnt DESC", new String[] {});
        ArrayList<String> suggestion = new ArrayList<>();
        if(cursor != null)
            cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
           suggestion.add(cursor.getString(1));
            cursor.moveToNext();
        }
        return suggestion;
    }

    private ArrayList<String> getCategorySuggestion(String inputTask){
        SQLiteDatabase reader = db.getReadableDatabase();
        // Get all task for today
        Cursor cursor = reader.rawQuery("select COUNT(category) AS CategoryCnt, Category from taskstime where date(end_time) > date('now', '-7 days') AND task LIKE ? GROUP BY task ORDER BY CategoryCnt, task DESC", new String[] {inputTask});
        if(cursor == null || cursor.getCount() == 0)
            cursor = reader.rawQuery("select COUNT(category) AS CategoryCnt, Category from taskstime where date(end_time) > date('now', '-7 days') GROUP BY category ORDER BY CategoryCnt DESC", new String[] {});
        if(cursor != null)
            cursor.moveToFirst();
        ArrayList<String> suggestion = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            suggestion.add(cursor.getString(1));
            cursor.moveToNext();
        }
        return suggestion;
    }
}