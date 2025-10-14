package com.example.dean_mobile;

public class Camera {
    private String link;
    private int bounding1;
    private int bounding2;

    public Camera(String link, int bounding1, int bounding2) {
        this.link = link;
        this.bounding1 = bounding1;
        this.bounding2 = bounding2;
    }

    public Camera() {
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getBounding1() {
        return bounding1;
    }

    public void setBounding1(int bounding1) {
        this.bounding1 = bounding1;
    }

    public int getBounding2() {
        return bounding2;
    }

    public void setBounding2(int bounding2) {
        this.bounding2 = bounding2;
    }
}
