package com.edgesoft.resulthour.Model;

public class BasicResultData {
    private String title,date,targetUrl,university;

    public BasicResultData() {

    }

    public BasicResultData(String title, String date, String targetUrl, String university) {
        this.title = title;
        this.date = date;
        this.targetUrl = targetUrl;
        this.university = university;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
