package com.example.gamemology.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Screenshot implements Parcelable {
    private int id;
    private String image;

    public Screenshot() {
    }

    public Screenshot(int id, String image) {
        this.id = id;
        this.image = image;
    }

    protected Screenshot(Parcel in) {
        id = in.readInt();
        image = in.readString();
    }

    public static final Creator<Screenshot> CREATOR = new Creator<Screenshot>() {
        @Override
        public Screenshot createFromParcel(Parcel in) {
            return new Screenshot(in);
        }

        @Override
        public Screenshot[] newArray(int size) {
            return new Screenshot[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(image);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Add this method to match what ScreenshotAdapter is expecting
    public String getImageUrl() {
        return image;  // Just returns the same image field
    }

}