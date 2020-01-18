package com.hub.dairy.models;

public class User {

    private String userId, name, email, farmName, phone;

    public User() {
    }

    public User(String userId, String name, String email, String farmName, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.farmName = farmName;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail(){return email;}

    public String getFarmName() {
        return farmName;
    }

    public String getPhone() {
        return phone;
    }

}
