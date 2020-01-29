package com.hub.dairy.models;

public class MilkProduce {

    private String produceId, userId, animalId, animalName, quantity, date, time, timeOfDay;

    public MilkProduce() {
    }

    public MilkProduce(String produceId, String userId, String animalId, String animalName,
                       String quantity, String date, String time, String timeOfDay) {
        this.produceId = produceId;
        this.userId = userId;
        this.animalId = animalId;
        this.animalName = animalName;
        this.quantity = quantity;
        this.date = date;
        this.time = time;
        this.timeOfDay = timeOfDay;
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

    public String getQuantity() {
        return quantity;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }
}
