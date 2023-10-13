package com.example.youcandoit_app.dto;

public class ScheduleDto {
    private int schedule_number;
    private String mem_id;
    private String schedule_title;
    private String schedule_startdate;
    private String schedule_enddate;
    private int schedule_reminder;
    private String schedule_repeat;
    private String schedule_success;

    public ScheduleDto(int schedule_number, String schedule_title, String schedule_startdate, String schedule_enddate, String schedule_success) {
        this.schedule_number = schedule_number;
        this.schedule_title = schedule_title;
        this.schedule_startdate = schedule_startdate;
        this.schedule_enddate = schedule_enddate;
        this.schedule_success = schedule_success;
    }

    public int getSchedule_number() {
        return schedule_number;
    }

    public void setSchedule_number(int schedule_number) {
        this.schedule_number = schedule_number;
    }

    public String getMem_id() {
        return mem_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }

    public String getSchedule_title() {
        return schedule_title;
    }

    public void setSchedule_title(String schedule_title) {
        this.schedule_title = schedule_title;
    }

    public String getSchedule_startdate() {
        return schedule_startdate;
    }

    public void setSchedule_startdate(String schedule_startdate) {
        this.schedule_startdate = schedule_startdate;
    }

    public String getSchedule_enddate() {
        return schedule_enddate;
    }

    public void setSchedule_enddate(String schedule_enddate) {
        this.schedule_enddate = schedule_enddate;
    }

    public int getSchedule_reminder() {
        return schedule_reminder;
    }

    public void setSchedule_reminder(int schedule_reminder) {
        this.schedule_reminder = schedule_reminder;
    }

    public String getSchedule_repeat() {
        return schedule_repeat;
    }

    public void setSchedule_repeat(String schedule_repeat) {
        this.schedule_repeat = schedule_repeat;
    }

    public String getSchedule_success() {
        return schedule_success;
    }

    public void setSchedule_success(String schedule_success) {
        this.schedule_success = schedule_success;
    }

}
