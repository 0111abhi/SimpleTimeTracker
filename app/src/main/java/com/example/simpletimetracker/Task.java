package com.example.simpletimetracker;

public class Task {

    public int startTime;
    public int endTime;
    public String taskDescription;
    public String category;

    public Task(int startTime, int endTime, String taskDescription, String category){
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskDescription = taskDescription;
        this.category = category;
    }
}
