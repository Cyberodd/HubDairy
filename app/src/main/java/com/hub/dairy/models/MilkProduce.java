package com.hub.dairy.models;

public class MilkProduce {

    private String userId, animalId, animalName, quantity, date;

    public MilkProduce() {
    }

    public MilkProduce(String userId, String animalId, String animalName, String quantity, String date) {
        this.userId = userId;
        this.animalId = animalId;
        this.animalName = animalName;
        this.quantity = quantity;
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public String getAnimalName() {
        return animalName;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getDate() {
        return date;
    }
}
