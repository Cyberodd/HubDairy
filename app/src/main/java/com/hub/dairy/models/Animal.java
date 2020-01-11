package com.hub.dairy.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Animal implements Parcelable {

    private String id, name, breed, location, gender, regDate, imageUrl;

    public Animal() {
    }

    public Animal(String id, String name, String breed, String location, String gender,
                  String regDate, String imageUrl) {
        this.id = id;
        this.name = name;
        this.breed = breed;
        this.location = location;
        this.gender = gender;
        this.regDate = regDate;
        this.imageUrl = imageUrl;
    }

    protected Animal(Parcel in) {
        id = in.readString();
        name = in.readString();
        breed = in.readString();
        location = in.readString();
        gender = in.readString();
        regDate = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(breed);
        dest.writeString(location);
        dest.writeString(gender);
        dest.writeString(regDate);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Animal> CREATOR = new Creator<Animal>() {
        @Override
        public Animal createFromParcel(Parcel in) {
            return new Animal(in);
        }

        @Override
        public Animal[] newArray(int size) {
            return new Animal[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBreed() {
        return breed;
    }

    public String getLocation() {
        return location;
    }

    public String getGender() {
        return gender;
    }

    public String getRegDate() {
        return regDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
