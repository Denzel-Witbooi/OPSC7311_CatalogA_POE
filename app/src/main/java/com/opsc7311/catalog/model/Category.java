package com.opsc7311.catalog.model;

import com.google.firebase.Timestamp;


/**
 * Model class for a single Category
 */

public class Category {

    private String name;            // category name
    private String amount;          // category goal
    private String userId;          // userId to link to a category for specific user
    private Timestamp timeAdded;    // Timestamp for time added
    private String userName;        // userName same purpose as userId
    private String imageUrl;        // stores the path of the image

    // default constructor
    public Category() {
    }

    public Category(String name, String amount, String userId, Timestamp timeAdded, String userName, String imageUrl) {
        this.name = name;
        this.amount = amount;
        this.userId = userId;
        this.timeAdded = timeAdded;
        this.userName = userName;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
