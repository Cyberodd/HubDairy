package com.hub.dairy.models;

public class Transaction {

    private String transId, animalId, cash, type, date, userId, time, prevTransId;
    private float quantity;

    public Transaction() {
    }

    public Transaction(String transId, String animalId, float quantity, String cash, String type,
                       String date, String userId, String time, String prevTransId) {
        this.transId = transId;
        this.animalId = animalId;
        this.quantity = quantity;
        this.cash = cash;
        this.type = type;
        this.date = date;
        this.userId = userId;
        this.time = time;
        this.prevTransId = prevTransId;
    }

    public String getTransId() {
        return transId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public float getQuantity() {
        return quantity;
    }

    public String getCash() {
        return cash;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }

    public String getTime() {
        return time;
    }

    public String getPrevTransId() {
        return prevTransId;
    }
}
