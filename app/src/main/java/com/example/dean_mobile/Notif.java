package com.example.dean_mobile;

public class Notif {
    public String date;
    public String message;
    public String time;


    public Notif(String message, String time, String date) {
        this.date = date;
        this.message = message;
        this.time = time;

    }

    public Notif() {
    }

    public String getTitle() {
        return message;
    }

    public void setTitle(String title) {
        this.message = title;
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
