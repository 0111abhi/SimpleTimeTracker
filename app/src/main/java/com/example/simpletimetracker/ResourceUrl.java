package com.example.simpletimetracker;

import static java.lang.Integer.parseInt;

public class ResourceUrl {
    public static final String READ_TODAY_ROWS_SQL = "SELECT strftime('%H', start_time) AS StartTimeHour,strftime('%M', start_time) AS StartTimeMins, strftime('%H', end_time) AS EndTimeHour, strftime('%M', end_time) AS EndTimeMins, task, category FROM TasksTime WHERE date(start_time) = date('now', 'localtime') ORDER BY start_time ASC";
    public static int GetTotalMinutes(String hours, String minutes)
    {
        return parseInt(hours) * 60 + parseInt(minutes);
    }
}
