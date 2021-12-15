package com.edgesoft.resulthour.Model;

public class QuestionPaper {
    private String date,title,url,university;

    public QuestionPaper() {

    }

    public QuestionPaper(String date, String title, String url, String university) {
        this.date = date;
        this.title = title;
        this.url = url;
        this.university = university;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
