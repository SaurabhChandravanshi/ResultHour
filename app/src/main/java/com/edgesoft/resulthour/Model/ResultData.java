package com.edgesoft.resulthour.Model;

public class ResultData {
    private String date,title, examId, universityName;

    public ResultData() {

    }

    public ResultData(String title, String date, String examId, String universityName) {
        this.date = date;
        this.title = title;
        this.examId = examId;
        this.universityName = universityName;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
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

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }
}
