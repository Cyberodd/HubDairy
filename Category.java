package com.hub.dairy.models;

public class Category {

    private String categoryId, categoryName, userId;

    public Category() {
    }

    public Category(String categoryId, String categoryName, String userId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.userId = userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getUserId() {
        return userId;
    }
}
