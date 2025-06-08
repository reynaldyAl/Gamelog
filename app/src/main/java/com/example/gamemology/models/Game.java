package com.example.gamemology.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Game implements Parcelable {
    private int id;
    private String name;
    private String released;
    private String backgroundImage;
    private double rating;
    private String description;
    private List<String> genres;
    private List<String> platforms;
    private List<Screenshot> screenshots;
    private boolean isFavorite;

    // Constructors
    public Game() {
    }

    public Game(int id, String name, String released, String backgroundImage,
                double rating, String description) {
        this.id = id;
        this.name = name;
        this.released = released;
        this.backgroundImage = backgroundImage;
        this.rating = rating;
        this.description = description;
    }

    // Parcelable implementation
    protected Game(Parcel in) {
        id = in.readInt();
        name = in.readString();
        released = in.readString();
        backgroundImage = in.readString();
        rating = in.readDouble();
        description = in.readString();
        genres = in.createStringArrayList();
        platforms = in.createStringArrayList();
        screenshots = in.createTypedArrayList(Screenshot.CREATOR);
        isFavorite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(released);
        dest.writeString(backgroundImage);
        dest.writeDouble(rating);
        dest.writeString(description);
        dest.writeStringList(genres);
        dest.writeStringList(platforms);
        dest.writeTypedList(screenshots);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    // Existing getters and setters
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

    public String getReleased() {
        return released;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public List<Screenshot> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshots = screenshots;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

