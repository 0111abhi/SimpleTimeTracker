package com.example.simpletimetracker;

import android.provider.BaseColumns;

public final class TasksDataContract {
    private TasksDataContract() {}

    public static class TasksEntry implements BaseColumns{
        public static final String TABLE_NAME = "TasksTime";
        public static final String KEY_STARTTIME = "start_time";
        public static final String KEY_ENDTIME = "end_time";
        public static final String KEY_TASK = "task";
    }
}
