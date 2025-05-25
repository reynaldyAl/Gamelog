package com.example.gamemology.models;

public class Screenshot {
    private int id;
    private String imageUrl;

    public Screenshot() {
    }

    public Screenshot(int id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}