package com.example.gamemology.models;

public class Trailer {
    private int id;
    private String name;
    private String thumbnailUrl;
    private String videoUrl;

    public Trailer(int id, String name, String thumbnailUrl, String videoUrl) {
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // This method is needed by TrailerAdapter
    public String getPreviewImage() {
        return thumbnailUrl;
    }
}