package com.hub.dairy.models;

public class MeatProduce {

    private String produceId, userId, animalId, animalName, date;
    private float quantity;

    public MeatProduce() {
    }

    public MeatProduce(String produceId, String userId, String animalId, String animalName,
                       float quantity, String date) {
        this.produceId = produceId;
        this.userId = userId;
        this.animalId = animalId;
        this.animalName = animalName;
        this.quantity = quantity;
        this.date = date;
    }

    public String getProduceId() {
        return produceId;
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

    public float getQuantity() {
        return quantity;
    }

    public String getDate() {
        return date;
    }
}
