package com.hub.dairy.models;

public class Transaction {

    private String transId, animalId, type, quantity, date, cash, time, userId;

    private Transaction() {
    }

    public Transaction(String transId, String animalId, String type, String quantity,
                       String date, String cash, String time, String userId) {
        this.transId = transId;
        this.animalId = animalId;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.cash = cash;
        this.time = time;
        this.userId = userId;
    }

    public String getTransId() {
        return transId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public String getType() {
        return type;
    }

    public String getQuantity() {
        return quantity;
    }

    private String getDate() {
        return date;
    }

    public String getCash() {
        return cash;
    }

    public String getTime() {
        return time;
    }

    public String getUserId() {
        return userId;
    }
}
