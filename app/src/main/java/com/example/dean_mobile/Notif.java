package com.example.dean_mobile;

public class Notif {
    public String title;
    public String time;
    public String date;

    public Notif(String title, String time, String date) {
        this.title = title;
        this.time = time;
        this.date = date;
    }

    public Notif() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
