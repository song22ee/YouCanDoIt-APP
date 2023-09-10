package com.example.youcandoit_app.dto;

import android.graphics.Bitmap;

public class GroupDto {
    // 간단하게 필요한 필드만 선언
    private int group_number;
    private String group_name;
    private String group_subject;
    private Bitmap group_image;

    public GroupDto(int group_number, String group_name, String group_subject, Bitmap group_image) {
        super();
        this.group_number = group_number;
        this.group_name = group_name;
        this.group_subject = group_subject;
        this.group_image = group_image;
    }

    public int getGroup_number() {
        return group_number;
    }

    public void setGroup_number(int group_number) {
        this.group_number = group_number;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_subject() {
        return group_subject;
    }

    public void setGroup_subject(String group_subject) {
        this.group_subject = group_subject;
    }

    public Bitmap getGroup_image() {
        return group_image;
    }

    public void setGroup_image(Bitmap group_image) {
        this.group_image = group_image;
    }
}
