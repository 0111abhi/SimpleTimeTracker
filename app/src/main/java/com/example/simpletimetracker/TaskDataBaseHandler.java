package com.example.simpletimetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDataBaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "TasksDb";

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TasksDataContract.TasksEntry.TABLE_NAME + " (" + TasksDataContract.TasksEntry.KEY_STARTTIME + " TEXT," + TasksDataContract.TasksEntry.KEY_ENDTIME + " TEXT," + TasksDataContract.TasksEntry.KEY_TASK + " TEXT," + TasksDataContract.TasksEntry.KEY_CATEGORY + " TEXT)";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TasksDataContract.TasksEntry.TABLE_NAME;

    public TaskDataBaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<2){
            final String ALTER_TABLE = "ALTER TABLE " + TasksDataContract.TasksEntry.TABLE_NAME + " ADD COLUMN " + TasksDataContract.TasksEntry.KEY_CATEGORY +" TEXT default ('Others')";
            db.execSQL(ALTER_TABLE);
        }
    }
}
