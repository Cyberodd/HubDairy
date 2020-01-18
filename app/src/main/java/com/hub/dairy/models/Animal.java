package com.hub.dairy.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Animal implements Parcelable {

    private String animalId, animalName, animalBreed, location, gender, regDate, imageUrl, category,
            status, availability, father, mother, userId;

    public Animal() {
    }

    public Animal(String animalId, String animalName, String animalBreed, String location,
                   String gender, String regDate, String imageUrl, String category, String status,
                   String availability, String father, String mother, String userId) {
        this.animalId = animalId;
        this.animalName = animalName;
        this.animalBreed = animalBreed;
        this.location = location;
        this.gender = gender;
        this.regDate = regDate;
        this.imageUrl = imageUrl;
        this.category = category;
        this.status = status;
        this.availability = availability;
        this.father = father;
        this.mother = mother;
        this.userId = userId;
    }

    protected Animal(Parcel in) {
        animalId = in.readString();
        animalName = in.readString();
        animalBreed = in.readString();
        location = in.readString();
        gender = in.readString();
        regDate = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        status = in.readString();
        availability = in.readString();
        father = in.readString();
        mother = in.readString();
        userId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(animalId);
        dest.writeString(animalName);
        dest.writeString(animalBreed);
        dest.writeString(location);
        dest.writeString(gender);
        dest.writeString(regDate);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeString(status);
        dest.writeString(availability);
        dest.writeString(father);
        dest.writeString(mother);
        dest.writeString(userId);
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

    public String getAnimalId() {
        return animalId;
    }

    public String getAnimalName() {
        return animalName;
    }

    public String getAnimalBreed() {
        return animalBreed;
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

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public String getAvailability() {
        return availability;
    }

    public String getFather() {
        return father;
    }

    public String getMother() {
        return mother;
    }

    public String getUserId() {
        return userId;
    }
}
