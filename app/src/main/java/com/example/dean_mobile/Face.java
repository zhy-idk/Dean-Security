package com.example.dean_mobile;

public class Face {
    private String name;
    private String image;
    private int registration;

    public Face(String name, String image, int registration) {
        this.name = name;
        this.image = image;
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
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getRegistration() {
        return registration;
    }

    public void setRegistration(int registration) {
        this.registration = registration;
    }
}

