package com.hub.dairy.models;

public class MilkProduce {

    private String produceId, userId, animalId, animalName, mrgQty, date, time, evnQty, totalQty, remQty;

    public MilkProduce() {
    }

    public MilkProduce(String produceId, String userId, String animalId, String animalName,
                        String mrgQty, String date, String time, String evnQty, String totalQty,
                       String remQty) {
        this.produceId = produceId;
        this.userId = userId;
        this.animalId = animalId;
        this.animalName = animalName;
        this.mrgQty = mrgQty;
        this.date = date;
        this.time = time;
        this.evnQty = evnQty;
        this.totalQty = totalQty;
        this.remQty = remQty;
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

    public String getMrgQty() {
        return mrgQty;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getEvnQty() {
        return evnQty;
    }

    public String getTotalQty() {
        return totalQty;
    }

    public String getRemQty() {
        return remQty;
    }
}
