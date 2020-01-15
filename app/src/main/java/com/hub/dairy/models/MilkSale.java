package com.hub.dairy.models;

public class MilkSale {

    private String transId, animalId, quantity, cash, type, date, userId, time;

    public MilkSale() {
    }

    public MilkSale(String transId, String animalId, String quantity, String cash, String type,
                    String date, String userId, String time) {
        this.transId = transId;
        this.animalId = animalId;
        this.quantity = quantity;
        this.cash = cash;
        this.type = type;
        this.date = date;
        this.userId = userId;
        this.time = time;
    }

    public String getTransId() {
        return transId;
    }

    public String getAnimalId() {
        return animalId;
    }

    public String getQuantity() {
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
}
