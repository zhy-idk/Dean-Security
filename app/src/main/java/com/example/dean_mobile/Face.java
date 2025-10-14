package com.example.dean_mobile;

public class Face {
    private String name;
    private String img;
    private String registration;

    public Face(String name, String image, String registration) {
        this.name = name;
        this.img = image;
        this.registration = registration;
    }

    public Face() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return img;
    }

    public void setImage(String image) {
        this.img = image;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }
}
