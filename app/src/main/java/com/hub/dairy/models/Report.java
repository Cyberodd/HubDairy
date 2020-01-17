package com.hub.dairy.models;

public class Report {

    private String reportId, date, userId;
    private int milkSales, animalSales, totalTransactions;
    private float totalCash;

    public Report() {
    }

    public Report(String reportId, int milkSales, int animalSales,
                  float totalCash, int totalTransactions, String date, String userId) {
        this.reportId = reportId;
        this.milkSales = milkSales;
        this.animalSales = animalSales;
        this.totalCash = totalCash;
        this.totalTransactions = totalTransactions;
        this.date = date;
        this.userId = userId;
    }

    public String getReportId() {
        return reportId;
    }

    public int getMilkSales() {
        return milkSales;
    }

    public int getAnimalSales() {
        return animalSales;
    }

    public float getTotalCash() {
        return totalCash;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public String getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }
}
